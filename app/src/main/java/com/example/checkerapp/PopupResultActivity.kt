package com.example.checkerapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PopupResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup_result)

        val tvResult: TextView = findViewById(R.id.tvResult)
        val btnDone: Button = findViewById(R.id.btnDone)

        tvResult.text = "Kalimat yang anda inputkan merupakan kalimat yang baik."

        btnDone.setOnClickListener {
            finish()
        }
    }
}