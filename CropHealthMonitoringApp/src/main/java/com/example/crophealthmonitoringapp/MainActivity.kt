package com.example.crophealthmonitoringapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
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
    private lateinit var chatButton: Button
    private lateinit var labels: List<String>
    private lateinit var diseaseInfo: JSONObject

    private val CHANNEL_ID = "disease_alert_channel"

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()
        userId = firebaseAuth.currentUser?.uid

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseReference = FirebaseDatabase.getInstance().reference

        // Bind UI elements
        imageView = findViewById(R.id.imageView)
        predictionTextView = findViewById(R.id.predictionTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        recommendationTextView = findViewById(R.id.recommendationTextView)
        captureButton = findViewById(R.id.captureButton)
        galleryButton = findViewById(R.id.galleryButton)
        chatButton = findViewById(R.id.chatButton)

        // Request permissions
        requestPermissions()

        // Load the TFLite model, labels, and disease information
        initializeModel()

        // Capture button click listener
        val takePictureIntent =
            registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
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
        val pickImageIntent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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

        // Chat button click listener
        chatButton.setOnClickListener {
            // Navigate to ExpertsListActivity
            val intent = Intent(this, ExpertsListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun requestPermissions() {
        // Camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        }

        // Notification permission (for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    200
                )
            }
        }
    }

    private fun initializeModel() {
        try {
            tflite = Interpreter(loadModelFile())
            loadLabels()
            loadDiseaseInfo()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(this.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
        return bitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
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
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }

        for (x in 0 until 224) {
            for (y in 0 until 224) {
                val pixel = resizedBitmap.getPixel(x, y)
                input[0][y][x][0] = (pixel shr 16 and 0xFF) / 255.0f
                input[0][y][x][1] = (pixel shr 8 and 0xFF) / 255.0f
                input[0][y][x][2] = (pixel and 0xFF) / 255.0f
            }
        }

        val output = Array(1) { FloatArray(labels.size) }
        tflite?.run(input, output)

        return output[0]
    }

    private fun displayResult(result: FloatArray) {
        val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1
        if (maxIndex != -1) {
            val label = labels[maxIndex]
            val confidence = result[maxIndex]
            val diseaseDetails = diseaseInfo.optJSONObject(label)
            val description = diseaseDetails?.optString("description", "No description available.")
            val recommendation =
                diseaseDetails?.optString("recommendation", "No recommendation available.")

            predictionTextView.text =
                "Prediction: $label (Confidence: ${"%.2f".format(confidence * 100)}%)"
            descriptionTextView.text = "Description: $description"
            recommendationTextView.text =
                "Recommendation:\n- ${recommendation?.replace(". ", "\n- ") ?: "No recommendation available."}"
        } else {
            predictionTextView.text = "Prediction could not be determined."
            descriptionTextView.text = ""
            recommendationTextView.text = ""
        }
    }
}
