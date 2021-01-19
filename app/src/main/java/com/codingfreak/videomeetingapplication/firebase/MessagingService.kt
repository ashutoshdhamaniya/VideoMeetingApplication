package com.codingfreak.videomeetingapplication.firebase

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.codingfreak.videomeetingapplication.activity.IncomingInvitationActivity
import com.codingfreak.videomeetingapplication.util.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val type: String? = remoteMessage.data.get(Constants.REMOTE_MESSAGE_TYPE)

        if (type != null) {
            if (type == Constants.REMOTE_MESSAGE_INVITATION) {
                val intent: Intent = Intent(this, IncomingInvitationActivity::class.java)
                intent.putExtra(
                    Constants.REMOTE_MESSAGE_MEETING_TYPE,
                    remoteMessage.data.get(Constants.REMOTE_MESSAGE_MEETING_TYPE)
                )
                intent.putExtra(
                    Constants.KEY_FIRST_NAME,
                    remoteMessage.data.get(Constants.KEY_FIRST_NAME)
                )
                intent.putExtra(
                    Constants.KEY_LAST_NAME,
                    remoteMessage.data.get(Constants.KEY_LAST_NAME)
                )
                intent.putExtra(
                    Constants.KEY_EMAIL,
                    remoteMessage.data.getValue(Constants.KEY_EMAIL)
                )
                intent.putExtra(
                    Constants.REMOTE_MESSAGE_INVITER_TOKEN,
                    remoteMessage.data.get(Constants.REMOTE_MESSAGE_INVITER_TOKEN)
                )
                intent.putExtra(
                    Constants.REMOTE_MESSAGE_MEETING_ROOM,
                    remoteMessage.data.get(Constants.REMOTE_MESSAGE_MEETING_ROOM)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else if (type == Constants.REMOTE_MESSAGE_INVITATION_RESPONSE) {
                val broadcastIntent: Intent = Intent(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)
                broadcastIntent.putExtra(
                    Constants.REMOTE_MESSAGE_INVITATION_RESPONSE,
                    remoteMessage.data.get(Constants.REMOTE_MESSAGE_INVITATION_RESPONSE)
                )
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(broadcastIntent)
            }
        }

        /*if(remoteMessage.notification != null) {
            Log.d(
                "FCM",
                "Remote Message Received ${remoteMessage.notification!!.body}"
            )
        }*/
    }

}