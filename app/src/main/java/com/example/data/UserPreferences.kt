package com.example.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("veil_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_SIMULATED_BLE = "key_simulated_ble"
        private const val KEY_AUTO_RECONNECT = "key_auto_reconnect"
        private const val DEFAULT_USER_NAME = "Alex Hunter"
    }

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, DEFAULT_USER_NAME) ?: DEFAULT_USER_NAME
        set(value) = prefs.edit().putString(KEY_USER_NAME, value.trim()).apply()

    var isSimulatedBle: Boolean
        get() = prefs.getBoolean(KEY_SIMULATED_BLE, false)
        set(value) = prefs.edit().putBoolean(KEY_SIMULATED_BLE, value).apply()

    var autoReconnect: Boolean
        get() = prefs.getBoolean(KEY_AUTO_RECONNECT, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_RECONNECT, value).apply()
}
