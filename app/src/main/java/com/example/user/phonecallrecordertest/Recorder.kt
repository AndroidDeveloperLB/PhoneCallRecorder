package com.example.user.phonecallrecordertest

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.IOException


class Recorder {
    private var mediaRecorder: MediaRecorder? = null
    var isRecording = false
    private var player: MediaPlayer? = null

    companion object {
        fun getFilePath(context: Context): String {
//            return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), "recording.mp3").absolutePath
//            return File(context.getExternalFilesDir("call_recording"), "recording.mp3").absolutePath
            return File(context.filesDir, "call_recording/recording.mp3").absolutePath;
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

    fun playRecording(context: Context) {
        if (player != null)
            stopPlayRecoding()
        val recordingFilePath = Recorder.getFilePath(context)
        Toast.makeText(context, "playing...", Toast.LENGTH_SHORT).show()
        Log.d("AppLog", "playing audio...")
        player = MediaPlayer()
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

    fun startRecording(context: Context) {
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

        val audioSource: AudioSource = getSavedAudioSource(context)
//        mediaRecorder!!.setAudioChannels(2)
        mediaRecorder!!.setAudioSource(audioSource.audioSourceValue)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
        val file = File(filepath)
        file.parentFile.mkdirs()
        if (file.exists())
            file.delete()
        mediaRecorder!!.setOutputFile(filepath)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        try {
            Log.d("AppLog", "preparing to record using audio source:$audioSource")
            mediaRecorder!!.prepare()
            Log.d("AppLog", "starting record")
            mediaRecorder!!.start()
            Log.d("AppLog", "started to record")
        } catch (e: Exception) {
            Log.e("AppLog", "error while recording:$e")
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
    }

    @Suppress("ProtectedInFinal")
    protected fun finalize() {
        stopRecording()
        stopPlayRecoding()
    }

}
