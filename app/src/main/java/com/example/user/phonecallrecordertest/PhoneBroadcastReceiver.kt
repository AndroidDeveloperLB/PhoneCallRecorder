package com.example.user.phonecallrecordertest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat

class PhoneBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AppLog", "PhoneBroadcastReceiver: ${intent.action} ${intent.getStringExtra(TelephonyManager.EXTRA_STATE)}");
        ContextCompat.startForegroundService(context, Intent(context, CallRecordingService::class.java).putExtra(CallRecordingService.EXTRA_PHONE_INTENT, intent))
    }

}
