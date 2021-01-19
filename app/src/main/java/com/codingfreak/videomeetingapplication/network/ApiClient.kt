package com.codingfreak.videomeetingapplication.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

/*
*   This will provide an instance of retrofit
*   at the time of network call
* */

class ApiClient {

    private var retrofit: Retrofit? = null

    fun getClient(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder().baseUrl("https://fcm.googleapis.com/fcm/")
                .addConverterFactory(ScalarsConverterFactory.create()).build()
        }
        return retrofit!!
    }

}