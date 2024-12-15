package com.example.checkerapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.net.InetAddress
import android.content.Context
import android.net.ConnectivityManager
import android.widget.ImageView

class InputActivity : AppCompatActivity() {
    // Create OkHttpClient as a class-level property
    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        val btnSubmit: Button = findViewById(R.id.btnSubmit)
        val etInput: EditText = findViewById(R.id.etInput)

        btnSubmit.setOnClickListener {
            val inputText = etInput.text.toString()
            if (inputText.isNotEmpty()) {
                // Pass the client directly
                sendTextToAPI(inputText, client)
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun sendTextToAPI(input: String, client: OkHttpClient) {
        // Gunakan URL terbaru dari ngrok
        val url = "https://034d-34-106-164-132.ngrok-free.app/predict"

        try {
            // Validasi URL
            val parsedUrl = URL(url)
            Log.d("API_REQUEST", "Parsed Host: ${parsedUrl.host}")
            Log.d("API_REQUEST", "Parsed Protocol: ${parsedUrl.protocol}")

            // Cek koneksi internet dan resolusi hostname
            val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            val isNetworkAvailable = networkInfo != null && networkInfo.isConnected
            Log.d("API_REQUEST", "Network Available: $isNetworkAvailable")

            // Hapus resolusi manual dengan InetAddress jika sudah menggunakan OkHttpClient dengan DNS SYSTEM
            // Cek koneksi internet menggunakan OkHttpClient

            // Create JSON request body
            val requestBody = JSONObject().put("text", input).toString()
                .toRequestBody("application/json".toMediaTypeOrNull())

            // Konfigurasi OkHttpClient dengan DNS.SYSTEM
            val robustClient = client.newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .dns(Dns.SYSTEM) // Menggunakan DNS sistem default
                .hostnameVerifier { _, _ -> true } // Longgar verifikasi hostname
                .build()

            // Create the request with additional headers
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .post(requestBody)
                .build()

            // Logging request
            Log.d("API_REQUEST", "Request URL: ${request.url}")
            Log.d("API_REQUEST", "Request Body: ${JSONObject().put("text", input)}")

            // Execute the request with error handling
            robustClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("API_REQUEST", "Request Failed: ${e.message}")
                    // Memastikan bahwa showResultPopup dijalankan di main thread
                    runOnUiThread {
                        showResultPopup("Connection Error: ${e.message ?: "Unknown Network Error"}", "Error")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    // Menangani response dan memastikan UI diperbarui di main thread
                    try {
                        val responseBodyString = response.body?.string() ?: "Empty Response"
                        Log.d("API_REQUEST", "Raw Response: $responseBodyString")

                        runOnUiThread {
                            if (response.isSuccessful) {
                                val jsonResponse = JSONObject(responseBodyString)
                                val confidence = jsonResponse.optDouble("confidence", 0.0)
                                val sentiment = jsonResponse.optString("sentiment", "Netral")
                                val sentenceEvaluation = when (sentiment) {
                                    "Positif" -> "Kalimat yang anda inputkan merupakan kalimat yang baik."
                                    "Negatif" -> "Kalimat yang anda inputkan merupakan kalimat yang tidak baik."
                                    else -> "Kalimat ini netral untuk digunakan"
                                }
                                showResultPopup("$sentenceEvaluation\nConfidence: ${String.format("%.2f", confidence)}%", "$sentiment")
                            } else {
                                showResultPopup("Server Error: ${response.code}", "Error")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("API_REQUEST", "Response Processing Error", e)
                        // Memastikan UI diperbarui di main thread
                        runOnUiThread {
                            showResultPopup("Unexpected Error: ${e.message}", "Error")
                        }
                    }
                }
            })

        } catch (e: Exception) {
            Log.e("API_REQUEST", "Setup Error", e)
            showResultPopup("Setup Error: ${e.message}", "Error")
        }
    }



    private fun showResultPopup(message: String, sentiment: String) {
        val dialogView = layoutInflater.inflate(R.layout.activity_popup_result, null)
        val tvResult: TextView = dialogView.findViewById(R.id.tvResult)
        val btnDone: Button = dialogView.findViewById(R.id.btnDone)
        val imageViewResult: ImageView = dialogView.findViewById(R.id.imageViewResult) // ImageView yang akan diubah

        tvResult.text = message

        // Periksa dan normalisasi pesan untuk memastikan kondisi lebih akurat
        when {
            sentiment.contains("Positif", ignoreCase = true) -> {
                imageViewResult.setImageResource(R.drawable.positif) // Jika Sentiment Positif
            }
            sentiment.contains("Negatif", ignoreCase = true) -> {
                imageViewResult.setImageResource(R.drawable.negatif) // Jika Sentiment Negatif
            }
            else -> {
                imageViewResult.setImageResource(R.drawable.negatif) // Jika sentiment tidak dikenali (netral)
            }
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
