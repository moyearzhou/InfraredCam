package com.moyear.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.moyear.R

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val KEY_SETTING_NAME = "setting_name"

        const val SETTING_ROOT = "root_setting"

        const val SETTING_CAMERA = "camera_setting"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            navigateToSetting(RootSettingFragment())
        }

        val settingName = intent.getStringExtra(KEY_SETTING_NAME) ?: SETTING_ROOT
        when(settingName) {
            SETTING_CAMERA -> navigateToSetting(CameraSettingFragment())
            else -> navigateToSetting(RootSettingFragment())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // 返回上一个Activity
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToSetting(settingFragment: PreferenceFragmentCompat) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, settingFragment)
            .commit()
    }


    class RootSettingFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            when (preference.key) {
                "view_demo" -> {
                    val intent = Intent(requireContext(), DemoActivity::class.java)
                    startActivity(intent)
                    return true
                }
                else -> return super.onPreferenceTreeClick(preference)
            }
        }
    }

    class CameraSettingFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.camera_preferences, rootKey)
        }

//        override fun onPreferenceTreeClick(preference: Preference): Boolean {
//            when (preference.key) {
//                "view_demo" -> {
//                    val intent = Intent(requireContext(), DemoActivity::class.java)
//                    startActivity(intent)
//                    return true
//                }
//                else -> return super.onPreferenceTreeClick(preference)
//            }
//        }
    }
}