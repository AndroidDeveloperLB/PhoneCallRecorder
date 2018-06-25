package com.example.user.phonecallrecordertest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    lateinit var recorder: Recorder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recorder = Recorder(this)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionsToRequest = getAppDeclaredPermissions(this)
            requestPermissions(permissionsToRequest, 0)
        }
        playRecordingButton.setOnClickListener {
            recorder.playRecording()
        }
        recordingSourceButton.setOnClickListener {
            val audioSources = AudioSource.getAllSupportedValues(this)
            val items = arrayOfNulls<CharSequence>(audioSources.size)
            for (i in 0 until audioSources.size)
                items[i] = audioSources[i].name
            AlertDialog.Builder(this@MainActivity).setTitle("choose recording source")
                    .setItems(items) { _, which ->
                        Recorder.setSavedAudioSource(this@MainActivity, audioSources[which])
                        updateRecordingSourceButton()
                    }.show()
        }
        callPhoneButton.setOnClickListener {
            val phone = phoneEditText.text.toString()
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("last_phone_entered", phone).apply()
            dialPhone(this@MainActivity, phone)
        }
        updateRecordingSourceButton()
        phoneEditText.setText(PreferenceManager.getDefaultSharedPreferences(this).getString("last_phone_entered", null))
//        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun updateRecordingSourceButton() {
        val audioSource: AudioSource = Recorder.getSavedAudioSource(this)
        recordingSourceButton.text = "recording source:${audioSource.name}"
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder.stopPlayRecoding()
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("last_phone_entered", phoneEditText.text.toString()).apply()
    }

    companion object {
        @SuppressLint("MissingPermission")
        @JvmStatic
        fun dialPhone(context: Context, phone: String) {
            context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone")))
        }

        @JvmStatic
        fun getAppDeclaredPermissions(context: Context): Array<out String>? {
            val pm = context.packageManager
            try {
                val packageInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
                return packageInfo.requestedPermissions ?: return null
            } catch (ignored: PackageManager.NameNotFoundException) {
                //we should always find current app
            }
            throw RuntimeException("cannot find current app?!")
        }

    }

}
