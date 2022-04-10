package com.example.user.phonecallrecordertest

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat

class CallRecordingService : Service() {
    lateinit var recorder: Recorder
    private var isIncomingCall = true

    companion object {
        const val EXTRA_PHONE_INTENT = "EXTRA_PHONE_INTENT"
        const val OUTGOING_CALLS_RECORDING_PREPARATION_DELAY_IN_MS = 1000L
    }

    override fun onCreate() {
        super.onCreate()
        recorder = Recorder(this)
        Log.d("AppLog", "Service Created")
        val notification = Notifications.Builder(this, R.string.channel_id__call_recording).setContentTitle(getString(R.string.notification_title__call_recording))
                .setSmallIcon(android.R.drawable.ic_notification_overlay).build()
        startForeground(Notifications.NOTIFICATION_ID__CALL_RECORDING, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callIntent = intent?.getParcelableExtra<Intent>(CallRecordingService.EXTRA_PHONE_INTENT)
        when {
            callIntent == null || callIntent.action == null || (callIntent.action != Intent.ACTION_NEW_OUTGOING_CALL && callIntent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) -> return super.onStartCommand(callIntent, flags, startId)
            callIntent.action == Intent.ACTION_NEW_OUTGOING_CALL -> {
                Log.d("AppLog", "outgoing call")
//                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
//                audioManager.mode=AudioManager.MODE_IN_COMMUNICATION
//                startRecording()
                isIncomingCall = false
                return super.onStartCommand(callIntent, flags, startId)
            }
            else -> {
                val state: String? = callIntent.getStringExtra(TelephonyManager.EXTRA_STATE)
                Log.d("AppLog", "onReceive:$state")
                when (state) {
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        Log.d("AppLog", "call started")
//                        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
//                        audioManager.isSpeakerphoneOn = true
                        startRecording()
//                        audioManager.isSpeakerphoneOn = false
//        https://stuff.mit.edu/afs/sipb/project/android/docs/reference/android/media/AudioManager.html#MODE_IN_COMMUNICATION
//        MODE_NORMAL, MODE_RINGTONE, MODE_IN_CALL or MODE_IN_COMMUNICATION
//        audioManager.mode = AudioManager.MODE_IN_CALL
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        Log.d("AppLog", "call stopped")
                        stopRecording()
                    }
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        isIncomingCall = true
                        Log.d("AppLog", "call ringing")
                    }
                }
                return super.onStartCommand(callIntent, flags, startId)
            }
        }
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AppLog", "service destroyed")
    }

    fun startRecording() {
        Log.d("AppLog", "about to start recording... isRecording?${recorder.isRecording}")
        recorder.startRecording(if (isIncomingCall) 0L else OUTGOING_CALLS_RECORDING_PREPARATION_DELAY_IN_MS)
//        recorder.startRecording(0L)

    }

    fun stopRecording() {
        Log.d("AppLog", "about to stop recording... isRecording?${recorder.isRecording}")
        stopForeground(true)
        stopSelf()
        recorder.stopRecording()
    }

}
