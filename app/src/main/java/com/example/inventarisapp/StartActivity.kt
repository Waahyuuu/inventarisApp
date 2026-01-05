package com.example.inventarisapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.inventarisapp.databinding.ActivityStartOpenBinding

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartOpenBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("inventaris_pref", MODE_PRIVATE)

        if (prefs.getBoolean("is_first_open", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityStartOpenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            prefs.edit().putBoolean("is_first_open", true).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

}
