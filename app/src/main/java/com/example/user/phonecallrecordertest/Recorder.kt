package com.example.user.phonecallrecordertest

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import java.io.File
import java.io.IOException


class Recorder(context: Context) {
    private val context: Context

    init {
        this.context = context.applicationContext ?: context
    }

    private var mediaRecorder: MediaRecorder? = null
    var isRecording = false
    private var player: MediaPlayer? = null
    private var audioSource: AudioSource? = null

    companion object {
        fun getFilePath(context: Context): String {
//            return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), "recording.amr").absolutePath
//            return File(context.getExternalFilesDir("call_recording"), "recording.amr").absolutePath
            return File(context.filesDir, "call_recording/recording.amr").absolutePath;
        }


        @JvmStatic
        fun getSavedAudioSource(context: Context): AudioSource {
            val audioSource: AudioSource = AudioSource.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString("recodringSource", AudioSource.VOICE_CALL.name))
            return audioSource
        }

        @JvmStatic
        fun setSavedAudioSource(context: Context, audioSource: AudioSource) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("recodringSource", audioSource.name).apply()
        }
    }

    fun playRecording() {
        if (player != null)
            stopPlayRecoding()
        val recordingFilePath = Recorder.getFilePath(context)
        Toast.makeText(context, "playing...", Toast.LENGTH_SHORT).show()
        Log.d("AppLog", "playing audio...")
        player = MediaPlayer()
        player!!.setOnInfoListener(object : MediaPlayer.OnInfoListener {
            override fun onInfo(p0: MediaPlayer?, what: Int, extra: Int): Boolean {
                Log.d("AppLog", "onInfo $what $extra")
                return false
            }
        })
        player!!.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(p0: MediaPlayer?, what: Int, extra: Int): Boolean {
                Log.d("AppLog", "onError $what $extra")
                return false
            }
        })
        try {
            player!!.setOnCompletionListener {
                Log.d("AppLog", "finished playing audio")
                Toast.makeText(context, "done playing", Toast.LENGTH_SHORT).show()
            }
            player!!.setDataSource(recordingFilePath)
            player!!.prepare()
            player!!.start()
            Toast.makeText(context, "playing...", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Log.d("AppLog", "failed to play audio :" + e)
            Toast.makeText(context, "error playing", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            player?.release()
            player = null
        }
    }

    fun stopPlayRecoding() {
        player?.release()
        player = null
    }

    @UiThread
    fun startRecording(delayToWaitForRecordingPreparation: Long = 0L) {
        if (isRecording)
            return
        isRecording = true
        val filepath = getFilePath(context)
        Log.d("AppLog", "About to record into $filepath")
        //Toast.makeText(getApplicationContext(), "Recorder_Sarted" + fname, Toast.LENGTH_LONG).show();
        if (mediaRecorder != null) {
            mediaRecorder!!.stop()
            mediaRecorder!!.reset()
            mediaRecorder!!.release()
        }
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setOnErrorListener(object : MediaRecorder.OnErrorListener {
            override fun onError(mp: MediaRecorder?, what: Int, extra: Int) {
                Log.d("AppLog", "onError $what $extra")
                stopRecording()
            }
        })
//        mediaRecorder!!.setOnInfoListener(object : MediaRecorder.OnInfoListener {
//            override fun onInfo(mp: MediaRecorder?, what: Int, extra: Int) {
//                Log.d("AppLog", "onInfo $what $extra")
//                stopRecording(context)
//            }
//        })
        audioSource = getSavedAudioSource(context)
        val audioSource: AudioSource = audioSource!!
        if (audioSource == AudioSource.MIC) {
            val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_IN_CALL
            audioManager.isSpeakerphoneOn = true
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0)
//            audioManager.setParameters("noise_suppression=off")
        }
//        mediaRecorder!!.setAudioChannels(2)
        mediaRecorder!!.setAudioSource(audioSource.audioSourceValue)

//        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//
//        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        //
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)


        val file = File(filepath)
        file.parentFile.mkdirs()
        if (file.exists())
            file.delete()
        mediaRecorder!!.setOutputFile(filepath)
        try {
            Log.d("AppLog", "preparing to record using audio source:$audioSource")
            mediaRecorder!!.prepare()
            val runnable = Runnable {
                if (mediaRecorder != null)
                    try {
                        Log.d("AppLog", "starting record")
                        mediaRecorder!!.start()
                        Log.d("AppLog", "started to record")
                        Toast.makeText(context, "started to record call", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("AppLog", "error while recording:$e")
                        mediaRecorder?.reset()
                        stopRecording()
                        e.printStackTrace()
                    }
            }
            if (delayToWaitForRecordingPreparation <= 0L)
                runnable.run()
            else
                Handler().postDelayed(runnable, delayToWaitForRecordingPreparation)
        } catch (e: Exception) {
            Log.e("AppLog", "error while preparing:$e")
            mediaRecorder?.reset()
            stopRecording()
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        if (!isRecording)
            return
        isRecording = false
        Log.d("AppLog", "stopping record process")
        if (mediaRecorder != null) {
            try {
                mediaRecorder!!.stop()
            } catch (e: IllegalStateException) {
            }
            mediaRecorder!!.release()
            mediaRecorder = null
        }
        Log.d("AppLog", "stopped record process")
        if (audioSource == AudioSource.MIC) {
            val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.setParameters("noise_suppression=auto")
        }
    }

    @Suppress("ProtectedInFinal")
    protected fun finalize() {
        stopRecording()
        stopPlayRecoding()
    }

}
