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
            pushActivity(MainActivity::class.java)
        }
        // FIXME
        //  besser nicht für jede Aufgabe eine neue Activity erstellen, sondern einfach in der
        //  MainActivity jeweils eine andere Funktion aufrufen
        binding.btnDistance.setOnClickListener {
            val snack = Snackbar.make(it, "Missing", Snackbar.LENGTH_SHORT)
            snack.show()
//            pushActivity(::class.java)
        }
        binding.btnEnergyEfficient.setOnClickListener {
            val snack = Snackbar.make(it, "Missing", Snackbar.LENGTH_SHORT)
            snack.show()
//            pushActivity(::class.java)
        }
        binding.btnStill.setOnClickListener {
            val snack = Snackbar.make(it, "Missing", Snackbar.LENGTH_SHORT)
            snack.show()
//            pushActivity(::class.java)
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