package com.lm22_23.prakti3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.lm22_23.prakti3.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val actionbar = supportActionBar
        actionbar!!.title = "Menü"

        initButtons()
    }

    private fun initButtons() {
        binding.btnPeriodic.setOnClickListener {
            val intent = Intent(this@MenuActivity, MainActivity::class.java)
            intent.putExtra("ReportingStrategy", ReportingStrategy.PERIODIC)
            startActivity(intent)
        }
        binding.btnDistance.setOnClickListener {
            val intent = Intent(this@MenuActivity, MainActivity::class.java)
            intent.putExtra("ReportingStrategy", ReportingStrategy.DISTANCE)
            startActivity(intent)
        }
        binding.btnEnergyEfficient.setOnClickListener {
            val intent = Intent(this@MenuActivity, MainActivity::class.java)
            intent.putExtra("ReportingStrategy", ReportingStrategy.ENERGY_EFFICIENT)
            startActivity(intent)
        }
        binding.btnStill.setOnClickListener {
            val intent = Intent(this@MenuActivity, MainActivity::class.java)
            intent.putExtra("ReportingStrategy", ReportingStrategy.STILL)
            startActivity(intent)
        }
        binding.btnSettings.setOnClickListener {
            pushActivity(SettingsActivity::class.java)
        }
    }

    private fun pushActivity(cls: Class<*>?) {
        val intent = Intent(this, cls)
        startActivity(intent)
    }
}