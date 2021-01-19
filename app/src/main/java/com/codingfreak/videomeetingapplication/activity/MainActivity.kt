package com.codingfreak.videomeetingapplication.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codingfreak.videomeetingapplication.R
import com.codingfreak.videomeetingapplication.adapter.UserAdapter
import com.codingfreak.videomeetingapplication.listeners.UsersListeners
import com.codingfreak.videomeetingapplication.model.User
import com.codingfreak.videomeetingapplication.util.Constants
import com.codingfreak.videomeetingapplication.util.PreferenceManager
import com.google.firebase.firestore.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), UsersListeners {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var userName: TextView
    private lateinit var signOut: TextView
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var errorText: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var imageConference: ImageView

    private lateinit var users: List<User>
    private lateinit var userAdapter: UserAdapter

    private val REQUEST_CODE_BATTERY_OPTIMIZTION: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //  Initialize fields

        preferenceManager = PreferenceManager(this)
        userName = findViewById(R.id.userName)
        signOut = findViewById(R.id.signOut)
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        errorText = findViewById(R.id.textErrorMessage)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        imageConference = findViewById(R.id.imageConference)

        userName.text = String.format(
            "%s %s",
            preferenceManager.getString(Constants.KEY_FIRST_NAME)?.substring(0, 1)
                ?.toUpperCase() + preferenceManager.getString(Constants.KEY_FIRST_NAME)
                ?.substring(1)?.toLowerCase(),
            preferenceManager.getString(Constants.KEY_LAST_NAME)?.substring(0, 1)
                ?.toUpperCase() + preferenceManager.getString(Constants.KEY_LAST_NAME)?.substring(1)
                ?.toLowerCase()
        )

        signOut.setOnClickListener { signOut() }

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if (it.isSuccessful && it.result != null) {
                sendFCMToeknToDatabase(it.result!!.token)
            }
        }

        swipeRefreshLayout.setOnRefreshListener(this::getAllUsers)

        users = ArrayList<User>()

        getAllUsers()

        userAdapter = UserAdapter(users, this)
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = userAdapter

        checkForBatteryOptimization()
    }

    private fun getAllUsers() {
        swipeRefreshLayout.isRefreshing = true
        val firestoreDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        firestoreDatabase.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener {
            swipeRefreshLayout.isRefreshing = false
            val userId: String = preferenceManager.getString(Constants.KEY_USER_ID).toString()
            if (it.isSuccessful && it.result != null) {
                (users as ArrayList<User>).clear()
                for (snapshot: QueryDocumentSnapshot in it.result!!) {
                    if (userId == snapshot.id) {
                        continue
                    }

                    val user: User = User(
                        snapshot.getString(Constants.KEY_FIRST_NAME).toString(),
                        snapshot.getString(Constants.KEY_LAST_NAME).toString(),
                        snapshot.getString(Constants.KEY_EMAIL).toString(),
                        snapshot.getString(Constants.KEY_FCM_TOKEN).toString()
                    )

                    (users as ArrayList<User>).add(user)
                }

                if (users.size > 0) {
                    userAdapter.notifyDataSetChanged()
                } else {
                    errorText.text = String.format("%s", "No Users Available")
                    errorText.visibility = View.VISIBLE
                }
            } else {
                errorText.text = String.format("%s", "No Users Available")
                errorText.visibility = View.VISIBLE
            }
        }
    }

    /*
    *   Getting user reference form database
    *   Sending FCM Token to database
    * */
    private fun sendFCMToeknToDatabase(token: String) {
        val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

        val documentReference: DocumentReference =
            firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)!!
            )

        documentReference.update(Constants.KEY_FCM_TOKEN, token).addOnFailureListener {
            Toast.makeText(this, "Unable to update token", Toast.LENGTH_SHORT).show()
        }
    }

    /*
    *   Sign out user
    *   clear saved preferences
    *   delete FCM token in database
    * */
    private fun signOut() {
        Toast.makeText(this, "Signing Out ...", Toast.LENGTH_SHORT).show()

        val firestoreRef: FirebaseFirestore = FirebaseFirestore.getInstance()
        val documentReference: DocumentReference =
            firestoreRef.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)!!
            )

        val updateDatabase: HashMap<String, Any> = HashMap()
        updateDatabase.put(Constants.KEY_FCM_TOKEN, FieldValue.delete())

        documentReference.update(updateDatabase).addOnSuccessListener {
            preferenceManager.clearPreference()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Unable To Sign Out", Toast.LENGTH_SHORT).show()
        }
    }

    override fun initiateVideoMeeting(user: User) {

        val token: String = user.token

        if (token == null.toString() || user.token.trim().isEmpty()) {
            Toast.makeText(
                this,
                user.firstName.substring(0, 1)
                    .toUpperCase(Locale.getDefault()) + user.firstName.substring(1)
                    .toLowerCase(Locale.getDefault()) + " " + user.lastName.substring(0, 1)
                    .toUpperCase(Locale.getDefault()) + user.lastName.substring(1)
                    .toLowerCase(Locale.getDefault()) + " is not available for meeting",
                Toast.LENGTH_SHORT
            ).show()
        } else {

            val videoMeetingIntent = Intent(this, OutgoingInvitationActivity::class.java)
            videoMeetingIntent.putExtra("user", user)
            videoMeetingIntent.putExtra("type", "video")

            startActivity(videoMeetingIntent)

        }
    }

    override fun initiateAudioMeeting(user: User) {

        val token: String = user.token

        if (token == null.toString() || user.token.trim().isEmpty()) {
            Toast.makeText(
                this,
                user.firstName.substring(0, 1)
                    .toUpperCase(Locale.getDefault()) + user.firstName.substring(1)
                    .toLowerCase(Locale.getDefault()) + " " + user.lastName.substring(0, 1)
                    .toUpperCase(Locale.getDefault()) + user.lastName.substring(1)
                    .toLowerCase(Locale.getDefault()) + " is not available for meeting",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val audioIntent: Intent = Intent(this, OutgoingInvitationActivity::class.java)
            audioIntent.putExtra("type", "audio")
            audioIntent.putExtra("user", user)
            startActivity(audioIntent)
        }
    }

    override fun onMultipleUsersAction(isMultipleUserSelected: Boolean) {
        if (isMultipleUserSelected) {
            imageConference.visibility = View.VISIBLE
            imageConference.setOnClickListener {
                val outgoingIntent: Intent = Intent(this, OutgoingInvitationActivity::class.java)
                outgoingIntent.putExtra("selectedUsers", Gson().toJson(UserAdapter.selectedUsers))
                outgoingIntent.putExtra("type", "video")
                outgoingIntent.putExtra("isMultiple", true)
                startActivity(outgoingIntent)
            }
        } else {
            imageConference.visibility = View.GONE
        }
    }

    private fun checkForBatteryOptimization() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val powerManager: PowerManager = getSystemService(POWER_SERVICE) as PowerManager

            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle("Warning")
                builder.setPositiveButton(
                    "Disable"
                ) { _, _ ->
                    val intent : Intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivityForResult(intent , REQUEST_CODE_BATTERY_OPTIMIZTION)
                }

                builder.setNegativeButton("Cancel") {dialog,_ -> dialog.dismiss() }


                builder.create().show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE_BATTERY_OPTIMIZTION) {
            checkForBatteryOptimization()
        }

    }
}