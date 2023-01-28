package com.lm22_23.prakti3

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.SeekBar
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

        initSeekBars(sharedPref)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initSeekBars(sharedPref: SharedPreferences) {
        // Periode
        val sbPeriodValue = sharedPref.getInt(PERIOD_MS, 1000)
        binding.sbPeriod.progress = sbPeriodValue
        var string = "$sbPeriodValue ms"
        binding.twPeriodMs.text = string

        binding.sbPeriod.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val string = "$progress ms"
                binding.twPeriodMs.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                with(sharedPref.edit()) {
                    putInt(PERIOD_MS, seekBar!!.progress)
                    apply()
                }
            }
        })

        // Distanz
        val sbDistanceValue = sharedPref.getInt(DISTANCE_M, 50)
        binding.sbDistance.progress = sbDistanceValue
        string = "$sbDistanceValue Meter"
        binding.twDistanceMeter.text = string

        binding.sbDistance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val string = "$progress Meter"
                binding.twDistanceMeter.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                with(sharedPref.edit()) {
                    putInt(DISTANCE_M, seekBar!!.progress)
                    apply()
                }
            }
        })

        // Abtastrate
        val sbSensingSpeedValue = sharedPref.getInt(SENSING_SPEED_MS, 10)
        binding.sbSensingSpeed.progress = sbSensingSpeedValue
        string = "$sbSensingSpeedValue ms"
        binding.twSensingSpeedMs.text = string

        binding.sbSensingSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val string = "$progress ms"
                binding.twSensingSpeedMs.text = string
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                with(sharedPref.edit()) {
                    putInt(SENSING_SPEED_MS, seekBar!!.progress)
                    apply()
                }
            }
        })
    }

    private fun saveRadioInfo(key: String, value: Boolean) {
        val sharedPref = getSharedPreferences(SETTINGS_PREFERENCES, Context.MODE_PRIVATE)

        with(sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }
}