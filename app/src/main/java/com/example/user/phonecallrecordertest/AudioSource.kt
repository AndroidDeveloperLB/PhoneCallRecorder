package com.example.user.phonecallrecordertest

import android.content.Context
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build

enum class AudioSource(val audioSourceValue: Int, val minApi: Int) {
    VOICE_CALL(MediaRecorder.AudioSource.VOICE_CALL, 4), DEFAULT(MediaRecorder.AudioSource.DEFAULT, 1), MIC(MediaRecorder.AudioSource.MIC, 1),
    CAMCORDER(MediaRecorder.AudioSource.CAMCORDER, 7), VOICE_COMMUNICATION(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 11),
    VOICE_RECOGNITION(MediaRecorder.AudioSource.VOICE_RECOGNITION, 7),
    VOICE_UPLINK(MediaRecorder.AudioSource.VOICE_UPLINK, 4), VOICE_DOWNLINK(MediaRecorder.AudioSource.VOICE_DOWNLINK, 4),
    REMOTE_SUBMIX(MediaRecorder.AudioSource.REMOTE_SUBMIX, 19),
    UNPROCESSED(MediaRecorder.AudioSource.UNPROCESSED, 24);

    fun isSupported(context: Context): Boolean {
        when {
            Build.VERSION.SDK_INT < minApi -> return false
            this != UNPROCESSED -> return true
            else -> {
                val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 ||
                        "true" == audioManager.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED))
                    return false
                return true
            }
        }
    }

    companion object {
        fun getAllSupportedValues(context: Context): ArrayList<AudioSource> {
            val values = AudioSource.values()
            val result = ArrayList<AudioSource>(values.size)
            for (value in values)
                if (value.isSupported(context))
                    result.add(value)
            return result
        }
    }

}
