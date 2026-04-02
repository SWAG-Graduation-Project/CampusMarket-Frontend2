package com.example.campusmarket

import android.content.Context

object GuestManager {

    private const val PREF_NAME = "app_prefs"
    private const val KEY_GUEST_UUID = "guestUuid"
    private const val KEY_MEMBER_ID = "memberId"

    fun getGuestUuid(context: Context): String? {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getString(KEY_GUEST_UUID, null)
    }

    fun getMemberId(context: Context): Long? {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val id = pref.getLong(KEY_MEMBER_ID, -1L)
        return if (id != -1L) id else null
    }

    fun isGuestReady(context: Context): Boolean {
        return getGuestUuid(context) != null
    }
}