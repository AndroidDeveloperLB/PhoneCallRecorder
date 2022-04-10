package com.example.user.phonecallrecordertest

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.user.phonecallrecordertest.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var recorder: Recorder
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this)).also { setContentView(it.root) }
        recorder = Recorder(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionsToRequest = getAppDeclaredPermissions(this)
            if (permissionsToRequest != null)
                requestPermissions(permissionsToRequest, 0)
        }
        binding.playRecordingButton.setOnClickListener {
            recorder.playRecording()
        }
        binding.recordingSourceButton2.setOnClickListener {
            val audioSources = AudioSource.getAllSupportedValues(this)
            val items = arrayOfNulls<CharSequence>(audioSources.size)
            for (i in 0 until audioSources.size)
                items[i] = audioSources[i].name
           MaterialAlertDialogBuilder(this@MainActivity).setTitle("choose recording source")
                .setItems(items) { _, which ->
                    Recorder.setSavedAudioSource(this@MainActivity, audioSources[which])
                    updateRecordingSourceButton()
                }.show()
        }
        binding.callPhoneButton.setOnClickListener {
            val phone = binding.phoneEditText.text.toString()
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("last_phone_entered", phone).apply()
            dialPhone(this@MainActivity, phone)
        }
        updateRecordingSourceButton()
        binding.phoneEditText.setText(PreferenceManager.getDefaultSharedPreferences(this)
            .getString("last_phone_entered", null))
        val deviceInfo = "${Build.MODEL};${Build.BRAND};${Build.DISPLAY};${Build.DEVICE};${Build.BOARD};${Build.HARDWARE};${Build.MANUFACTURER};${Build.ID}" +
                ";${Build.PRODUCT};${Build.VERSION.RELEASE};${Build.VERSION.SDK_INT};${Build.VERSION.INCREMENTAL};${Build.VERSION.CODENAME}"
        Log.d("AppLog", deviceInfo)
        binding.openAccessibilitySettings.setOnClickListener {
            requestAccessibilityPermission()
        }
    }

    fun updateRecordingSourceButton() {
        val audioSource: AudioSource = Recorder.getSavedAudioSource(this)
        binding.recordingSourceButton2.text = "recording source:${audioSource.name}"
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder.stopPlayRecoding()
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putString("last_phone_entered", binding.phoneEditText.text.toString()).apply()
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

    private fun requestAccessibilityPermission() {
        var intent = Intent("com.samsung.accessibility.installed_service")
        if (intent.resolveActivity(packageManager) == null) {
            intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        }
        val extraFragmentArgKey = ":settings:fragment_args_key"
        val extraShowFragmentArguments = ":settings:show_fragment_args"
        val bundle = Bundle()
        val showArgs: String = "${packageName}/${MyAccessibilityService::class.java.name}"
        bundle.putString(extraFragmentArgKey, showArgs)
        intent.putExtra(extraFragmentArgKey, showArgs)
        intent.putExtra(extraShowFragmentArguments, bundle)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY))
        }
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
