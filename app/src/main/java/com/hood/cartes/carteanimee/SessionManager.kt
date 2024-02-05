package com.hood.cartes.carteanimee

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "MyPrefs",
        Context.MODE_PRIVATE
    )

    var isLoggedIn: Boolean
        get() = sharedPreferences.getBoolean(IS_LOGGED_IN_KEY, false)
        set(value) = sharedPreferences.edit().putBoolean(IS_LOGGED_IN_KEY, value).apply()

    var userId: String?
        get() = sharedPreferences.getString(USER_ID_KEY, null)
        set(value) = sharedPreferences.edit().putString(USER_ID_KEY, value).apply()

    var prenomUser: String?
        get() = sharedPreferences.getString(USER_PRENOM, null)
        set(value) = sharedPreferences.edit().putString(USER_PRENOM, value).apply()
    var emailUser: String?
        get() = sharedPreferences.getString(USER_EMAIL, null)
        set(value) = sharedPreferences.edit().putString(USER_EMAIL, value).apply()
    var nomUser: String?
        get() = sharedPreferences.getString(USER_NOM, null)
        set(value) = sharedPreferences.edit().putString(USER_NOM, value).apply()

    var roleIdUser: String?
        get() = sharedPreferences.getString(USER_ROLE_ID, null)
        set(value) = sharedPreferences.edit().putString(USER_ROLE_ID, value).apply()
    var roleUser: String?
        get() = sharedPreferences.getString(USER_ROLE, null)
        set(value) = sharedPreferences.edit().putString(USER_ROLE, value).apply()

    companion object {
        private const val IS_LOGGED_IN_KEY = "isLoggedIn"
        private const val USER_ID_KEY = "userId"
        private const val USER_PRENOM = "prenomUser"
        private const val USER_EMAIL = "emailUser"
        private const val USER_NOM = "nomUser"
        private const val USER_ROLE_ID = "roleIdUser"
        private const val USER_ROLE = "roleUser"
    }
}
