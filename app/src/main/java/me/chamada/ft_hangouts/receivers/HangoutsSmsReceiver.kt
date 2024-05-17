package me.chamada.ft_hangouts.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class HangoutsSmsReceiver: BroadcastReceiver() {
    companion object {
        const val TAG = "HangoutsSmsReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            when (it.action) {
                Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                    val messages = Telephony.Sms.Intents.getMessagesFromIntent(it)

                    Log.d(TAG, "Received ${messages.count()} messages!")
                }
                else -> {
                    Log.w(TAG, "Unknown action: ${it.action}")
                }
            }
        }
    }
}