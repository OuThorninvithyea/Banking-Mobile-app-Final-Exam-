package com.hustle.bankapp.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("hustlebank_prefs", Context.MODE_PRIVATE)

    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired = _sessionExpired.asSharedFlow()

    companion object {
        private const val KEY_TOKEN = "jwt_token"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun notifySessionExpired() {
        clearToken()
        _sessionExpired.tryEmit(Unit)
    }
}
