package com.codingfreak.videomeetingapplication.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.codingfreak.videomeetingapplication.R
import com.codingfreak.videomeetingapplication.model.User
import com.codingfreak.videomeetingapplication.network.ApiClient
import com.codingfreak.videomeetingapplication.network.ApiService
import com.codingfreak.videomeetingapplication.util.Constants
import com.codingfreak.videomeetingapplication.util.PreferenceManager
import com.google.common.reflect.TypeToken
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.lang.Exception
import java.lang.StringBuilder
import java.lang.reflect.Type
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class OutgoingInvitationActivity : AppCompatActivity() {

    private lateinit var meetingType: ImageView
    private lateinit var userNameFirstChar: TextView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var rejectButton: ImageView
    private var user : User? = null

    private lateinit var preferenceManager: PreferenceManager
    private var inviterToken: String? = null
    private var meetingRoom: String? = null
    private var typeFromIntent : String? = null

    private var rejectionCount : Int = 0
    private var totalReceivers : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outgoing_invitation)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //  Initialize Fields

        meetingType = findViewById(R.id.imageMeetingType)
        userNameFirstChar = findViewById(R.id.textUserNameFirstCharacter)
        userName = findViewById(R.id.textUserName)
        userEmail = findViewById(R.id.textUserEmail)
        rejectButton = findViewById(R.id.outgoingRejectButton)
        preferenceManager = PreferenceManager(this)

        //  Receiving Data From Intent



        user = intent.getSerializableExtra("user") as? User

        typeFromIntent = intent.getStringExtra("type")

        //  Setting The Data
        if(typeFromIntent != null) {
            if (typeFromIntent == "video") {
                meetingType.setBackgroundResource(R.drawable.ic_video)
            } else if(typeFromIntent == "audio") {
                meetingType.setBackgroundResource(R.drawable.ic_call)
            }
        }

        if(user != null){
            userNameFirstChar.text = user!!.firstName.substring(0, 1).toUpperCase(Locale.getDefault())
            userName.text = String.format(
                "%s %s",
                user!!.firstName.substring(0, 1).toUpperCase(Locale.getDefault()) +
                        user!!.firstName
                            .substring(1)
                            .toLowerCase(Locale.getDefault()), user!!.lastName.substring(0, 1)
                    .toUpperCase(Locale.getDefault()) + user!!.lastName.substring(1)
                    .toLowerCase(Locale.getDefault())
            )

            userEmail.text = user?.email
        }

        rejectButton.setOnClickListener {
            if(intent.getBooleanExtra("isMultiple" , false)) {
                val type : Type = object : TypeToken<ArrayList<User>>(){}.type
                val receivers : ArrayList<User> = Gson().fromJson(intent.getStringExtra("selectedUsers") , type)
                cancelInvitation(null , receivers)
            } else {
                if(user != null) {
                    cancelInvitation(user?.token , null)
                }
            }
        }

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if (it.isSuccessful && it.result != null) {
                inviterToken = it.result!!.token

                if(typeFromIntent != null) {
                    if(intent.getBooleanExtra("isMultiple" , false)) {
                        val type : Type = object : TypeToken<ArrayList<User>>(){}.type
                        val receivers : ArrayList<User> = Gson().fromJson(intent.getStringExtra("selectedUsers") , type)
                        if(receivers != null) {
                            totalReceivers = receivers.size
                        }
                        initiateMeeting(typeFromIntent!! , null , receivers)
                    } else {
                        totalReceivers = 1;
                        initiateMeeting(typeFromIntent!!, user!!.token , null)
                    }
                }
            }
        }
    }

    private fun initiateMeeting(meetingType: String, receiverToken: String?, receivers : ArrayList<User>?) {
        try {

            val tokens: JSONArray = JSONArray()

            if(receiverToken != null) {
                tokens.put(receiverToken)
            }

            if(receivers != null && receivers.size > 0) {
                val userNames : StringBuilder = StringBuilder()

                for(user : User in receivers) {
                    tokens.put(user.token)
                    userNames.append(user.firstName).append(" ").append(user.lastName).append("\n")
                }

                userNameFirstChar.visibility = View.GONE
                userEmail.visibility = View.GONE
                userName.text = userNames
            }

            val body: JSONObject = JSONObject()
            val data: JSONObject = JSONObject()

            data.put(Constants.REMOTE_MESSAGE_TYPE, Constants.REMOTE_MESSAGE_INVITATION)
            data.put(Constants.REMOTE_MESSAGE_MEETING_TYPE, meetingType)
            data.put(
                Constants.KEY_FIRST_NAME,
                preferenceManager.getString(Constants.KEY_FIRST_NAME)
            )
            data.put(Constants.KEY_LAST_NAME, preferenceManager.getString(Constants.KEY_LAST_NAME))
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL))
            data.put(Constants.REMOTE_MESSAGE_INVITER_TOKEN, inviterToken)

            meetingRoom =
                preferenceManager.getString(Constants.KEY_USER_ID) + "_" + UUID.randomUUID()
                    .toString().substring(0, 5)

            data.put(Constants.REMOTE_MESSAGE_MEETING_ROOM, meetingRoom)


            body.put(Constants.REMOTE_MESSAGE_DATA, data)
            body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), Constants.REMOTE_MESSAGE_INVITATION)

        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun cancelInvitation(receiverToken: String? , receivers : ArrayList<User>?) {

        try {

            val tokens: JSONArray = JSONArray()

            if(receiverToken != null) {
                tokens.put(receiverToken)
            }

            if(receivers != null && receivers.size > 0) {
                for(user :User in receivers) {
                    tokens.put(user.token)
                }
            }

            val body: JSONObject = JSONObject()
            val data: JSONObject = JSONObject()

            data.put(Constants.REMOTE_MESSAGE_TYPE, Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)
            data.put(
                Constants.REMOTE_MESSAGE_INVITATION_RESPONSE,
                Constants.REMOTE_MESSAGE_INVITATION_CANCEL
            )

            body.put(Constants.REMOTE_MESSAGE_DATA, data)
            body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)

        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    private fun sendRemoteMessage(remoteMessageBody: String, type: String) {
        ApiClient().getClient().create(ApiService::class.java).sendRemoteMessage(
            Constants.getHeaders(), remoteMessageBody
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: retrofit2.Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    if (type == Constants.REMOTE_MESSAGE_INVITATION) {
                        Toast.makeText(
                            this@OutgoingInvitationActivity,
                            "Invitation Send Successful",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (type == Constants.REMOTE_MESSAGE_INVITATION_RESPONSE) {
                        Toast.makeText(
                            this@OutgoingInvitationActivity,
                            "Invitation Cancelled",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@OutgoingInvitationActivity,
                        response.message(),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onFailure(call: retrofit2.Call<String>, t: Throwable) {
                Toast.makeText(this@OutgoingInvitationActivity, t.message, Toast.LENGTH_SHORT)
                    .show()
                finish()
            }

        })
    }

    private val invitationResponseReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val type: String? = p1!!.getStringExtra(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)

            if (type != null) {
                if (type == Constants.REMOTE_MESSAGE_INVITATION_ACCEPTED) {
                    Toast.makeText(
                        this@OutgoingInvitationActivity,
                        "Invitation Accepted",
                        Toast.LENGTH_SHORT
                    ).show()

                    try {

                        val serverUrl: URL = URL("https://meet.jit.si")

                        val builder : JitsiMeetConferenceOptions.Builder = JitsiMeetConferenceOptions.Builder()
                        builder.setServerURL(serverUrl)
                        builder.setWelcomePageEnabled(false)
                        builder.setRoom(meetingRoom)

                        if(meetingType.equals("audio")) {
                            builder.setVideoMuted(true)
                        }

                        JitsiMeetActivity.launch(this@OutgoingInvitationActivity , builder.build())
                        finish()

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@OutgoingInvitationActivity, e.message, Toast.LENGTH_SHORT
                        ).show()
                    }

                } else if (type == Constants.REMOTE_MESSAGE_INVITATION_REJECTED) {
                    rejectionCount += 1

                    if(totalReceivers == rejectionCount) {
                        Toast.makeText(
                            this@OutgoingInvitationActivity,
                            "Invitation Rejected",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(
                    this@OutgoingInvitationActivity,
                    "Some Error Occur",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            invitationResponseReceiver,
            IntentFilter(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            invitationResponseReceiver
        )
    }
}