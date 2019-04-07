package com.example.user.phonecallrecordertest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
            if (permissionsToRequest != null)
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
            androidx.appcompat.app.AlertDialog.Builder(this@MainActivity).setTitle("choose recording source")
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

//        Log.d("AppLog", "${audioManager.getParameters("channels")}")
        val deviceInfo = "${Build.MODEL};${Build.BRAND};${Build.DISPLAY};${Build.DEVICE};${Build.BOARD};${Build.HARDWARE};${Build.MANUFACTURER};${Build.ID}" +
                ";${Build.PRODUCT};${Build.VERSION.RELEASE};${Build.VERSION.SDK_INT};${Build.VERSION.INCREMENTAL};${Build.VERSION.CODENAME}"
        Log.d("AppLog", deviceInfo)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var url: String? = null
        when (item.itemId) {
            R.id.menuItem_all_my_apps -> url = "https://play.google.com/store/apps/developer?id=AndroidDeveloperLB"
            R.id.menuItem_all_my_repositories -> url = "https://github.com/AndroidDeveloperLB"
            R.id.menuItem_current_repository_website -> url = "https://github.com/AndroidDeveloperLB/PhoneCallRecorder"
        }
        if (url == null)
            return true
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        startActivity(intent)
        return true
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
