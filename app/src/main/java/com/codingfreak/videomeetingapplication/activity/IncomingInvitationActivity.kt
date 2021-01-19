package com.codingfreak.videomeetingapplication.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.codingfreak.videomeetingapplication.R
import com.codingfreak.videomeetingapplication.network.ApiClient
import com.codingfreak.videomeetingapplication.network.ApiService
import com.codingfreak.videomeetingapplication.util.Constants
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.lang.Exception
import java.net.URL
import java.util.*

class IncomingInvitationActivity : AppCompatActivity() {

    private lateinit var meetingType: ImageView
    private lateinit var userNameFirstChar: TextView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var acceptInvitation: ImageView
    private lateinit var rejectInvitation: ImageView
    private var type : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_invitation)

        //  Initialize Fields

        meetingType = findViewById(R.id.imageMeetingType)
        userNameFirstChar = findViewById(R.id.textUserNameFirstCharacter)
        userName = findViewById(R.id.textUserName)
        userEmail = findViewById(R.id.textUserEmail)
        acceptInvitation = findViewById(R.id.meetingInvitationAccept)
        rejectInvitation = findViewById(R.id.meetingInvitationReject)

        //  Receive Data From Intent and set

        type = intent.getStringExtra(Constants.REMOTE_MESSAGE_MEETING_TYPE)

        userNameFirstChar.text =
            intent.getStringExtra(Constants.KEY_FIRST_NAME)!!.substring(0, 1).toUpperCase(
                Locale.getDefault()
            )

        if (type != null) {
            if (type == "video") {
                meetingType.setBackgroundResource(R.drawable.ic_video)
            } else if (type == "audio") {
                meetingType.setBackgroundResource(R.drawable.ic_call)
            }
        }

        userName.text = String.format(
            "%s %s", intent.getStringExtra(Constants.KEY_FIRST_NAME)!!.substring(0, 1).toUpperCase(
                Locale.getDefault()
            ) + intent.getStringExtra(Constants.KEY_FIRST_NAME)!!.substring(1)
                .toLowerCase(Locale.getDefault()),
            intent.getStringExtra(Constants.KEY_LAST_NAME)!!.substring(0, 1)
                .toUpperCase(Locale.getDefault()) + intent.getStringExtra(
                Constants.KEY_LAST_NAME
            )!!.substring(
                1
            )
                .toLowerCase(
                    Locale.getDefault()
                )
        )
        userEmail.text = intent.getStringExtra(Constants.KEY_EMAIL)

        acceptInvitation.setOnClickListener {
            sendInvitationResponse(
                Constants.REMOTE_MESSAGE_INVITATION_ACCEPTED,
                intent.getStringExtra(Constants.REMOTE_MESSAGE_INVITER_TOKEN)!!
            )
        }

        rejectInvitation.setOnClickListener {
            sendInvitationResponse(
                Constants.REMOTE_MESSAGE_INVITATION_REJECTED,
                intent.getStringExtra(Constants.REMOTE_MESSAGE_INVITER_TOKEN)!!
            )
        }

    }

    private fun sendInvitationResponse(type: String, receiverToken: String) {

        try {

            val tokens: JSONArray = JSONArray()
            tokens.put(receiverToken)

            val body: JSONObject = JSONObject()
            val data: JSONObject = JSONObject()

            data.put(Constants.REMOTE_MESSAGE_TYPE, Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)
            data.put(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE, type)

            body.put(Constants.REMOTE_MESSAGE_DATA, data)
            body.put(Constants.REMOTE_MESSAGE_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), type)

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
                    if (type == Constants.REMOTE_MESSAGE_INVITATION_ACCEPTED) {

                        Toast.makeText(this@IncomingInvitationActivity , "Invitation Accepted" , Toast.LENGTH_SHORT).show()

                        try {

                            val serverUrl: URL = URL("https://meet.jit.si")

                            val builder : JitsiMeetConferenceOptions.Builder = JitsiMeetConferenceOptions.Builder()
                            builder.setServerURL(serverUrl)
                            builder.setWelcomePageEnabled(false)
                            builder.setRoom(intent.getStringExtra(Constants.REMOTE_MESSAGE_MEETING_ROOM))

                            if(type == "audio") {
                                builder.setVideoMuted(true)
                            }

                            JitsiMeetActivity.launch(this@IncomingInvitationActivity , builder.build())
                            finish()

                        } catch (e: Exception) {
                            Toast.makeText(this@IncomingInvitationActivity , e.message , Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(
                            this@IncomingInvitationActivity,
                            "Invitation Rejected",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@IncomingInvitationActivity,
                        response.message(),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onFailure(call: retrofit2.Call<String>, t: Throwable) {
                Toast.makeText(this@IncomingInvitationActivity, t.message, Toast.LENGTH_SHORT)
                    .show()
                finish()
            }

        })
    }

    private var invitationResponseReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val type: String? = p1!!.getStringExtra(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)

            if (type != null) {
                if (type == Constants.REMOTE_MESSAGE_INVITATION_CANCEL) {
                    Toast.makeText(
                        this@IncomingInvitationActivity,
                        "Invitation Cancelled",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            invitationResponseReceiver,
            IntentFilter(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(invitationResponseReceiver)
    }
}