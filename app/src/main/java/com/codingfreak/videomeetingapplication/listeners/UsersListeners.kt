package com.codingfreak.videomeetingapplication.listeners

import com.codingfreak.videomeetingapplication.model.User

interface UsersListeners {

    fun initiateVideoMeeting(user : User)

    fun initiateAudioMeeting(user : User)

    fun onMultipleUsersAction(isMultipleUserSelected : Boolean)

}