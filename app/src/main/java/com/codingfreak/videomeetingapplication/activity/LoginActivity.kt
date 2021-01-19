package com.codingfreak.videomeetingapplication.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import com.codingfreak.videomeetingapplication.R
import com.codingfreak.videomeetingapplication.util.AESEncryption
import com.codingfreak.videomeetingapplication.util.Constants
import com.codingfreak.videomeetingapplication.util.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text
import kotlin.math.log

class LoginActivity : AppCompatActivity() {

    private lateinit var signUpText: TextView
    private lateinit var emailErrorText: TextView
    private lateinit var passwordErrorText: TextView
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: MaterialButton
    private lateinit var loginProgressBar: ProgressBar

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //  Initialize Fields

        signUpText = findViewById(R.id.signUpText)
        emailErrorText = findViewById(R.id.emailErrorText)
        passwordErrorText = findViewById(R.id.passwordErrorText)
        loginEmail = findViewById(R.id.loginEmail)
        loginPassword = findViewById(R.id.loginPassword)
        loginButton = findViewById(R.id.loginButton)
        loginProgressBar = findViewById(R.id.loginProgressBar)

        preferenceManager = PreferenceManager(this)

        //  Check user is already sign in or not

        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            val mainIntent : Intent = Intent(this , MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }

        // Sign up Activity Intent

        signUpText.setOnClickListener {
            val signUpActivityIntent: Intent =
                Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(signUpActivityIntent)
        }

        //  On Clicking Login Button

        loginButton.setOnClickListener {

            val email: String = loginEmail.text.trim().toString()
            val password: String = loginPassword.text.trim().toString()

            if (email.isEmpty()) {
                emailErrorText.text = "Enter Email"
                emailErrorText.visibility = View.VISIBLE
                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailErrorText.text = "Enter Valid Email"
                emailErrorText.visibility = View.VISIBLE
                return@setOnClickListener
            } else if (password.isEmpty()) {
                passwordErrorText.text = "Enter Password"
                passwordErrorText.visibility = View.VISIBLE
                return@setOnClickListener
            } else {
                emailErrorText.visibility = View.INVISIBLE
                passwordErrorText.visibility = View.INVISIBLE

                signIn(email, password)
            }
        }

        //  Add Dummy data to firebase firestore for testing purpose

        /*
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val user: HashMap<String, Any> = HashMap()
        user.put("first_name", "Ashutosh")
        user.put("last_name", "Dhamaniya")
        user.put("email", "ashutosh@gmail.com")

        database.collection("users").add(user).addOnSuccessListener {
            object : OnSuccessListener<DocumentReference> {
                override fun onSuccess(p0: DocumentReference?) {
                    Toast.makeText(this@LoginActivity , "User Inserted" , Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            object : OnFailureListener {
                override fun onFailure(p0: Exception) {
                    Toast.makeText(this@LoginActivity , "Error in adding a user" , Toast.LENGTH_SHORT).show()
                }
            }
        }
        */

    }

    private fun signIn(email: String, password: String) {
        loginButton.visibility = View.INVISIBLE
        loginProgressBar.visibility = View.VISIBLE

        val firestoreRef: FirebaseFirestore = FirebaseFirestore.getInstance()

        firestoreRef.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, email).whereEqualTo(Constants.KEY_PASSWORD, AESEncryption().encrypt(password))
            .get().addOnCompleteListener {
                run {
                    if (it.isSuccessful && it.result != null && it.result!!.documents.size > 0) {
                        val documentSnapshot: DocumentSnapshot = it.result!!.documents[0]
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                        preferenceManager.putString(Constants.KEY_USER_ID , documentSnapshot.id)
                        preferenceManager.putString(
                            Constants.KEY_FIRST_NAME,
                            documentSnapshot.getString(Constants.KEY_FIRST_NAME)!!
                        )
                        preferenceManager.putString(
                            Constants.KEY_LAST_NAME,
                            documentSnapshot.getString(Constants.KEY_LAST_NAME)!!
                        )
                        preferenceManager.putString(
                            Constants.KEY_EMAIL,
                            documentSnapshot.getString(Constants.KEY_EMAIL)!!
                        )

                        val mainIntent: Intent = Intent(this, MainActivity::class.java)
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(mainIntent)

                    } else {
                        loginButton.visibility = View.VISIBLE
                        loginProgressBar.visibility = View.INVISIBLE
                        Toast.makeText(this, " Unable To Sign In ", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}