package com.example.crophealthmonitoringapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.tensorflow.lite.Interpreter
import org.json.JSONObject
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var predictionTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var recommendationTextView: TextView
    private var tflite: Interpreter? = null
    private lateinit var captureButton: Button
    private lateinit var galleryButton: Button
    private lateinit var yesButton: Button
    private lateinit var noButton: Button
    private lateinit var chatButton: Button
    private lateinit var labels: List<String>
    private lateinit var diseaseInfo: JSONObject

    private val CHANNEL_ID = "disease_alert_channel"
    private val NOTIFICATION_ID = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        imageView = findViewById(R.id.imageView)
        predictionTextView = findViewById(R.id.predictionTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        recommendationTextView = findViewById(R.id.recommendationTextView)
        captureButton = findViewById(R.id.captureButton)
        galleryButton = findViewById(R.id.galleryButton)
        yesButton = findViewById(R.id.button_yes)
        noButton = findViewById(R.id.button_no)
        chatButton = findViewById(R.id.chatButton) // Ensure this exists in the XML layout

        // Initially hide the feedback buttons
        yesButton.visibility = Button.GONE
        noButton.visibility = Button.GONE

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 200)
            }
        }

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Disease Alert Channel"
            val descriptionText = "Channel for disease detection alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Load the TFLite model, labels, and disease information
        try {
            Log.d("ModelLoading", "Attempting to load the model...")
            tflite = Interpreter(loadModelFile())
            Log.d("ModelLoading", "Model loaded successfully.")
            loadLabels()
            Log.d("LabelsLoading", "Labels loaded successfully: $labels")
            loadDiseaseInfo()
            Log.d("DiseaseInfoLoading", "Disease information loaded successfully.")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("ModelLoading", "Failed to load the model, labels, or disease information: ${e.message}")
        }

        // Capture button click listener
        val takePictureIntent = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                imageView.setImageBitmap(it)
                val result = runInference(it)
                displayResult(result)
            }
        }

        captureButton.setOnClickListener {
            takePictureIntent.launch(null)
        }

        // Gallery button click listener
        val pickImageIntent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val bitmap = loadBitmapFromUri(it)
                imageView.setImageBitmap(bitmap)
                val result = runInference(bitmap)
                displayResult(result)
            }
        }

        galleryButton.setOnClickListener {
            pickImageIntent.launch("image/*")
        }

        // Yes button click listener
        yesButton.setOnClickListener {
            predictionTextView.text = "${predictionTextView.text}\n\nThank you for your feedback!"
            yesButton.visibility = Button.GONE
            noButton.visibility = Button.GONE
        }

        // No button click listener
        noButton.setOnClickListener {
            predictionTextView.text = "${predictionTextView.text}\n\nThank you for your feedback! We will work to improve the model."
            yesButton.visibility = Button.GONE
            noButton.visibility = Button.GONE
        }

        // Chat button click listener
        chatButton.setOnClickListener {
            val expertId = "expert_001" // Replace this with a dynamic value if needed
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("expertId", expertId)
            Log.d("MainActivity", "Passing Expert ID: $expertId")
            startActivity(intent)
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(this.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
        return bitmap.copy(Bitmap.Config.ARGB_8888, true) // Convert to a format that supports pixel access
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabels() {
        val labelsInput = assets.open("labels.txt")
        labels = labelsInput.bufferedReader().readLines()
        labelsInput.close()
    }

    private fun loadDiseaseInfo() {
        val diseaseInfoInput = assets.open("disease_info.json")
        val size = diseaseInfoInput.available()
        val buffer = ByteArray(size)
        diseaseInfoInput.read(buffer)
        diseaseInfoInput.close()
        val jsonString = String(buffer, Charset.defaultCharset())
        diseaseInfo = JSONObject(jsonString)
    }

    private fun runInference(bitmap: Bitmap): FloatArray {
        Log.d("Inference", "Starting inference...")

        // Resize the bitmap to the required input size of the model (224x224 pixels)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }

        for (x in 0 until 224) {
            for (y in 0 until 224) {
                val pixel = resizedBitmap.getPixel(x, y)
                input[0][y][x][0] = (pixel shr 16 and 0xFF) / 255.0f // Red channel
                input[0][y][x][1] = (pixel shr 8 and 0xFF) / 255.0f  // Green channel
                input[0][y][x][2] = (pixel and 0xFF) / 255.0f         // Blue channel
            }
        }

        val output = Array(1) { FloatArray(labels.size) } // Output size should match the number of labels
        tflite?.run(input, output)

        Log.d("Inference", "Inference run successfully with output: ${output[0].contentToString()}")

        return output[0]
    }

    private fun displayResult(result: FloatArray) {
        val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1
        if (maxIndex != -1) {
            val label = labels[maxIndex]
            val confidence = result[maxIndex]
            val diseaseDetails = diseaseInfo.optJSONObject(label)
            val description = diseaseDetails?.optString("description", "No description available.")
            val recommendation = diseaseDetails?.optString("recommendation", "No recommendation available.")

            when {
                confidence >= 0.90 -> {
                    predictionTextView.text = "Prediction: $label (Confidence: ${"%.2f".format(confidence * 100)}%)"
                    descriptionTextView.text = "Description: $description"
                    recommendationTextView.text = "Recommendation:\n- ${recommendation?.replace(". ", "\n- ") ?: "No recommendation available."}"
                    sendHighConfidenceNotification(label, confidence)
                }
                confidence >= 0.70 -> {
                    predictionTextView.text = "Prediction: $label (Confidence: ${"%.2f".format(confidence * 100)}%)"
                    descriptionTextView.text = "Description: $description"
                    recommendationTextView.text = "Recommendation:\n- ${recommendation?.replace(". ", "\n- ") ?: "No recommendation available."}"
                }
                confidence >= 0.50 -> {
                    predictionTextView.text = "Prediction: $label (Confidence: ${"%.2f".format(confidence * 100)}%)"
                    descriptionTextView.text = "The model's confidence in this prediction is moderate. We recommend consulting an agricultural expert or trying to upload a clearer image."
                    recommendationTextView.text = ""
                }
                else -> {
                    predictionTextView.text = "The model is unsure about the prediction. Please consult an agricultural expert or try uploading a clearer image."
                    descriptionTextView.text = ""
                    recommendationTextView.text = ""
                }
            }

            Log.d("Result", "Prediction: $label, Confidence: ${"%.2f".format(confidence * 100)}%")

            // Show the feedback buttons only if the confidence is 50% or higher
            if (confidence >= 0.50) {
                yesButton.visibility = Button.VISIBLE
                noButton.visibility = Button.VISIBLE
            } else {
                yesButton.visibility = Button.GONE
                noButton.visibility = Button.GONE
            }
        } else {
            predictionTextView.text = "Prediction could not be determined."
            descriptionTextView.text = ""
            recommendationTextView.text = ""
            Log.e("Result", "Prediction could not be determined.")
        }
    }

    private fun sendHighConfidenceNotification(label: String, confidence: Float) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("High Confidence Disease Detection")
            .setContentText("Detected $label with ${"%.2f".format(confidence * 100)}% confidence. Immediate action is recommended.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }
}
