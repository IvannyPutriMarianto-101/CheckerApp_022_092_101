package com.example.checkerapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class InputActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        val btnSubmit: Button = findViewById(R.id.btnSubmit)
        val etInput: EditText = findViewById(R.id.etInput)

        btnSubmit.setOnClickListener {
            val inputText = etInput.text.toString()
            if (inputText.isNotEmpty()) {
                showResultPopup(inputText)
            }
        }
    }

    private fun showResultPopup(input: String) {
        val dialogView = layoutInflater.inflate(R.layout.activity_popup_result, null)
        val tvResult: TextView = dialogView.findViewById(R.id.tvResult)
        val btnDone: Button = dialogView.findViewById(R.id.btnDone)

        val isGoodSentence = checkSentence(input)
        tvResult.text = if (isGoodSentence) {
            "Kalimat yang anda inputkan merupakan kalimat yang baik."
        } else {
            "Kalimat yang anda inputkan merupakan kalimat yang tidak baik."
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnDone.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkSentence(input: String): Boolean {
        return input.split(" ").size > 5
    }
}
