package com.lm22_23.prakti3

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lm22_23.prakti3.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val actionbar = supportActionBar
        actionbar!!.title = "Einstellungen"
        actionbar.setDisplayHomeAsUpEnabled(true)

        val sharedPref = getSharedPreferences(SETTINGS_PREFERENCES, Context.MODE_PRIVATE)

        val rbRoute1 = binding.rbRoute1
        val rbRoute2 = binding.rbRoute2
        val rbRoute3 = binding.rbRoute3

        rbRoute1.isChecked = sharedPref.getBoolean(ROUTE_1, false)
        rbRoute2.isChecked = sharedPref.getBoolean(ROUTE_2, false)
        rbRoute3.isChecked = sharedPref.getBoolean(ROUTE_3, false)

        rbRoute1.setOnCheckedChangeListener{
                _, isChecked -> saveRadioInfo(ROUTE_1, isChecked)
        }
        rbRoute2.setOnCheckedChangeListener{
                _, isChecked -> saveRadioInfo(ROUTE_2, isChecked)
        }
        rbRoute3.setOnCheckedChangeListener{
                _, isChecked -> saveRadioInfo(ROUTE_3, isChecked)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun saveRadioInfo(key: String, value: Boolean) {
        val sharedPref = getSharedPreferences(SETTINGS_PREFERENCES, Context.MODE_PRIVATE)

        with(sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }
}