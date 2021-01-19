package com.codingfreak.videomeetingapplication.model

import java.io.Serializable

class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val token: String
) : Serializable