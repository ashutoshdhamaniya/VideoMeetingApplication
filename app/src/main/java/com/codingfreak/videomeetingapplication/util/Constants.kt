package com.codingfreak.videomeetingapplication.util

class Constants {

    companion object {
        val KEY_COLLECTION_USERS: String = "users"
        val KEY_FIRST_NAME: String = "first_name"
        val KEY_LAST_NAME: String = "last_name"
        val KEY_EMAIL: String = "email"
        val KEY_PASSWORD: String = "password"
        val KEY_USER_ID: String = "user_id"
        val KEY_FCM_TOKEN: String = "fcm_token"

        val KEY_PREFERENCE_NAME: String = "videoMeetingPreference"
        val KEY_IS_SIGNED_IN: String = "isSignedIn"

        val REMOTE_MESSAGE_AUTHORIZATION: String = "Authorization"
        val REMOTE_MESSAGE_CONTENT_TYPE: String = "Content-Type"

        val REMOTE_MESSAGE_TYPE : String = "type"
        val REMOTE_MESSAGE_INVITATION : String = "invitation"
        val REMOTE_MESSAGE_MEETING_TYPE : String = "meetingType"
        val REMOTE_MESSAGE_INVITER_TOKEN : String = "inviterToken"
        val REMOTE_MESSAGE_DATA : String = "data"
        val REMOTE_MESSAGE_REGISTRATION_IDS : String = "registration_ids"

        val REMOTE_MESSAGE_INVITATION_RESPONSE : String = "invitationResponse"
        val REMOTE_MESSAGE_INVITATION_ACCEPTED : String = "accepted"
        val REMOTE_MESSAGE_INVITATION_REJECTED : String = "rejected"
        val REMOTE_MESSAGE_INVITATION_CANCEL : String = "cancelled"

        val REMOTE_MESSAGE_MEETING_ROOM : String = "meetingRoom"

        fun getHeaders(): HashMap<String, String> {
            val headers: HashMap<String, String> = HashMap<String, String>()

            headers[Constants.REMOTE_MESSAGE_AUTHORIZATION] =
                "key=AAAALTXrr-E:APA91bGg-y5kd1iXKqxyYrMVMZGHJ1FbCTOwNe-Q65vWCVcwefV6Sg2e5mT570wNnQpYJC25T1YYSVpwR5iYgGLQL0yBuWvvWa49hXdEFngAMRY7z9ii7C4eD_3BdULf3_DLg7hhKTbK"
            headers[Constants.REMOTE_MESSAGE_CONTENT_TYPE] = "application/json"

            return headers
        }
    }

}