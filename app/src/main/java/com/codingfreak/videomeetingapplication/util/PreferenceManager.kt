package com.codingfreak.videomeetingapplication.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(val context: Context) {

    private var sharedPreferences : SharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME , Context.MODE_PRIVATE)
    private val sharedPreferencesEditor : SharedPreferences.Editor = sharedPreferences.edit()

    fun putBoolean(key : String , value : Boolean) {
        sharedPreferencesEditor.putBoolean(key , value)
        sharedPreferencesEditor.apply()
    }

    fun getBoolean (key : String) : Boolean {
        return sharedPreferences.getBoolean(key , false)
    }

    fun putString (key : String , value : String) {
        sharedPreferencesEditor.putString(key , value)
        sharedPreferencesEditor.apply()
    }

    fun getString (key : String) : String? {
        return sharedPreferences.getString(key , null)
    }

    fun clearPreference () {
        sharedPreferencesEditor.clear()
        sharedPreferencesEditor.apply()
    }
}