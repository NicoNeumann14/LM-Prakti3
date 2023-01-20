package com.example.lm_rhn_prakti2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.lm_rhn_prakti2.databinding.ActivityMenuBinding

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
            pushActivity(MainActivity::class.java)
        }
        binding.btnDistance.setOnClickListener {
            pushActivity(MainActivity::class.java)
        }
        binding.btnEnergyEfficient.setOnClickListener {
            pushActivity(MainActivity::class.java)
        }
        binding.btnStill.setOnClickListener {
            pushActivity(MainActivity::class.java)
        }
        binding.btnSettings.setOnClickListener {
            pushActivity(MainActivity::class.java)
        }
    }

    private fun pushActivity(cls: Class<*>?) {
        val intent = Intent(this, cls)
        startActivity(intent)
    }
}