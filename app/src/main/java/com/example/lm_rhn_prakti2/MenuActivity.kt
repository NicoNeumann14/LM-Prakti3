package com.example.lm_rhn_prakti2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.lm_rhn_prakti2.databinding.ActivityMenuBinding
import com.google.android.material.snackbar.Snackbar

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
            val snack = Snackbar.make(it,"Missing", Snackbar.LENGTH_SHORT)
            snack.show()
//            pushActivity(::class.java)
        }
        binding.btnEnergyEfficient.setOnClickListener {
            val snack = Snackbar.make(it,"Missing", Snackbar.LENGTH_SHORT)
            snack.show()
//            pushActivity(::class.java)
        }
        binding.btnStill.setOnClickListener {
            val snack = Snackbar.make(it,"Missing", Snackbar.LENGTH_SHORT)
            snack.show()
//            pushActivity(::class.java)
        }
        binding.btnSettings.setOnClickListener {
            val snack = Snackbar.make(it,"Missing", Snackbar.LENGTH_SHORT)
            snack.show()
//            pushActivity(::class.java)
        }
    }

    private fun pushActivity(cls: Class<*>?) {
        val intent = Intent(this, cls)
        startActivity(intent)
    }
}