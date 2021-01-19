package com.codingfreak.videomeetingapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.codingfreak.videomeetingapplication.R
import com.codingfreak.videomeetingapplication.listeners.UsersListeners
import com.codingfreak.videomeetingapplication.model.User
import com.codingfreak.videomeetingapplication.util.Constants
import java.util.*
import kotlin.collections.ArrayList

class UserAdapter(private var usersList: List<User>, private val usersListeners: UsersListeners) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    companion object {
        var selectedUsers: ArrayList<User> = ArrayList()
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val firstChar: TextView = itemView.findViewById(R.id.userNameFirstChar)
        private val userName: TextView = itemView.findViewById(R.id.userNameTextField)
        private val userEmail: TextView = itemView.findViewById(R.id.userEmailTextField)
        private val videoCall: ImageView = itemView.findViewById(R.id.videoCall)
        private val voiceCall: ImageView = itemView.findViewById(R.id.voiceCall)
        private val userContainer: ConstraintLayout = itemView.findViewById(R.id.userContainer)
        private val imageSelected: ImageView = itemView.findViewById(R.id.selectedImage)

        fun setUserData(user: User, usersListeners: UsersListeners) {
            firstChar.text = user.firstName.substring(0, 1)
            userName.text = String.format(
                "%s %s",
                user.firstName.substring(0, 1)
                    .toUpperCase(Locale.getDefault()) + user.firstName.substring(1)
                    .toLowerCase(Locale.getDefault()),
                user.lastName.substring(0, 1)
                    .toUpperCase(Locale.getDefault()) + user.lastName.substring(1)
                    .toLowerCase(Locale.getDefault())
            )

            userEmail.text = user.email

            videoCall.setOnClickListener {
                usersListeners.initiateVideoMeeting(user)
            }

            voiceCall.setOnClickListener {
                usersListeners.initiateAudioMeeting(user)
            }

            userContainer.setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(p0: View?): Boolean {
                    if (imageSelected.visibility != View.VISIBLE) {
                        selectedUsers.add(user)
                        imageSelected.visibility = View.VISIBLE
                        videoCall.visibility = View.INVISIBLE
                        voiceCall.visibility = View.INVISIBLE
                        usersListeners.onMultipleUsersAction(true)
                    }
                    return true
                }
            })

            userContainer.setOnClickListener {
                if (imageSelected.visibility == View.VISIBLE) {
                    selectedUsers.remove(user)
                    imageSelected.visibility = View.GONE
                    voiceCall.visibility = View.VISIBLE
                    videoCall.visibility = View.VISIBLE

                    if (selectedUsers.size == 0) {
                        usersListeners.onMultipleUsersAction(false)
                    }
                } else {
                    if (selectedUsers.size > 0) {
                        selectedUsers.add(user)
                        imageSelected.visibility = View.VISIBLE
                        videoCall.visibility = View.INVISIBLE
                        voiceCall.visibility = View.INVISIBLE

                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.container_single_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserData(usersList[position], usersListeners)

    }

    override fun getItemCount(): Int {
        return usersList.size
    }

}