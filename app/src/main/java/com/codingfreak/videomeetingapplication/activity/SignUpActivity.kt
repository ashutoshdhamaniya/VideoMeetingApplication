package com.codingfreak.videomeetingapplication.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import com.codingfreak.videomeetingapplication.R
import com.codingfreak.videomeetingapplication.model.User
import com.codingfreak.videomeetingapplication.util.AESEncryption
import com.codingfreak.videomeetingapplication.util.Constants
import com.codingfreak.videomeetingapplication.util.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import org.w3c.dom.Document
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {

    private lateinit var loginText: TextView
    private lateinit var backButton: ImageView
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var firstNameError: TextView
    private lateinit var lastNameError: TextView
    private lateinit var emailError: TextView
    private lateinit var passwordError: TextView
    private lateinit var confirmPasswordError: TextView
    private lateinit var createAccountButton: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        //  Initialize Fields

        loginText = findViewById(R.id.loginText)
        backButton = findViewById(R.id.toolbarBackButton)
        firstName = findViewById(R.id.signUpFirstName)
        lastName = findViewById(R.id.signUpLastName)
        email = findViewById(R.id.signUpEmail)
        password = findViewById(R.id.signUpPassword)
        confirmPassword = findViewById(R.id.signUpConfirmPassword)
        firstNameError = findViewById(R.id.firstNameErrorText)
        lastNameError = findViewById(R.id.lastNameErrorText)
        emailError = findViewById(R.id.emailErrorText)
        passwordError = findViewById(R.id.passwordErrorText)
        confirmPasswordError = findViewById(R.id.confirmPasswordErrorText)
        createAccountButton = findViewById(R.id.createAccountButton)
        progressBar = findViewById(R.id.signUpProgressBar)

        preferenceManager = PreferenceManager(this)

        //  Toolbar Back Button

        backButton.setOnClickListener {
            onBackPressed()
        }

        //  Login Activity Intent

        loginText.setOnClickListener {
            val loginActivityIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginActivityIntent)
        }

        //  On Clicking CreateAccountButton

        createAccountButton.setOnClickListener {
            if (firstName.text.trim().toString().isEmpty()) {
                firstNameError.text = "Enter First Name"
                firstNameError.visibility = View.VISIBLE
                return@setOnClickListener
            } else if (lastName.text.trim().toString().isEmpty()) {
                lastNameError.text = "Enter Last Name"
                lastNameError.visibility = View.VISIBLE
                return@setOnClickListener
            } else if (email.text.trim().toString().isEmpty()) {
                emailError.text = "Enter Your Email Address"
                emailError.visibility = View.VISIBLE
                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
                emailError.text = "Enter a Valid Email Address"
                emailError.visibility = View.VISIBLE
            } else if (password.text.trim().toString().isEmpty()) {
                passwordError.text = "Enter Password"
                passwordError.visibility = View.VISIBLE
            } else if (confirmPassword.text.trim().toString().isEmpty()) {
                confirmPasswordError.text = "Confirm Your Password"
                confirmPasswordError.visibility = View.VISIBLE
            } else if (!password.text.trim().toString()
                    .equals(confirmPassword.text.trim().toString())
            ) {
                confirmPasswordError.text = "Password & Confirm Password Must Be same"
                confirmPasswordError.visibility = View.VISIBLE
            } else {
                firstNameError.visibility = View.INVISIBLE
                lastNameError.visibility = View.INVISIBLE
                emailError.visibility = View.INVISIBLE
                passwordError.visibility = View.INVISIBLE
                confirmPasswordError.visibility = View.INVISIBLE

                signUp()
            }
        }

    }

    private fun signUp() {
        createAccountButton.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE

        /*
        *   Create user account
        *   Enter user data into firebase firestore
        *   Manage Shared Preferences
        *
        * */

        val databaseRef: FirebaseFirestore = FirebaseFirestore.getInstance()
        val user = HashMap<String, Any>()
        user[Constants.KEY_FIRST_NAME] = firstName.text.trim().toString()
        user[Constants.KEY_LAST_NAME] = lastName.text.trim().toString()
        user[Constants.KEY_EMAIL] = email.text.trim().toString()
        user[Constants.KEY_PASSWORD] = AESEncryption().encrypt(password.text.trim().toString())

        databaseRef.collection(Constants.KEY_COLLECTION_USERS).get().addOnSuccessListener {
            var check: Boolean = true
            for (document: DocumentSnapshot in it.documents) {
                if (document.getString(Constants.KEY_EMAIL).equals(email.text.toString())) {
                    Toast.makeText(
                        this,
                        "Email Already Exist ${document.getString(Constants.KEY_EMAIL)}",
                        Toast.LENGTH_LONG
                    ).show()
                    progressBar.visibility = View.INVISIBLE
                    createAccountButton.visibility = View.VISIBLE
                    check = false
                    break
                }
            }

            if (check) {
                databaseRef.collection(Constants.KEY_COLLECTION_USERS).add(user)
                    .addOnSuccessListener {

                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                        preferenceManager.putString(Constants.KEY_USER_ID, it.id)
                        preferenceManager.putString(
                            Constants.KEY_FIRST_NAME,
                            firstName.text.toString()
                        )
                        preferenceManager.putString(
                            Constants.KEY_LAST_NAME,
                            lastName.text.toString()
                        )
                        preferenceManager.putString(Constants.KEY_EMAIL, email.text.toString())

                        val mainIntent: Intent = Intent(this, MainActivity::class.java)
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(mainIntent)

                    }.addOnFailureListener {
                        progressBar.visibility = View.INVISIBLE
                        createAccountButton.visibility = View.VISIBLE

                        Toast.makeText(this, "Error ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}

