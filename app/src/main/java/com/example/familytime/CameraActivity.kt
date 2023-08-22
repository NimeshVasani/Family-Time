package com.example.familytime

import com.example.familytime.adapters.CameraImageAdapter
import com.example.familytime.databinding.ActivityCameraBinding


import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import java.io.File
import java.io.Serializable

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var adapter: CameraImageAdapter
    private lateinit var imageUri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpAdapter()
        openCamera()

        binding.cameraImageCaptureBtn.setOnClickListener {
            captureImage()
            Toast.makeText(this, "clicked", Toast.LENGTH_LONG).show()
        }

        binding.cameraCloseButton.setOnClickListener {
            finish()
        }


        binding.cameraImageSendBtn.setOnClickListener {

            val resultIntent = Intent()
            resultIntent.data = imageUri

            setResult(RESULT_OK, resultIntent)

            finish()
        }


    }

    private fun setUpAdapter() {
        adapter = CameraImageAdapter()
        binding.cameraAllImageRecycler.adapter = adapter
        binding.cameraAllImageRecycler.setHasFixedSize(true)

        binding.cameraAllImageRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun openCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Set up the Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // Set up the ImageCapture
            imageCapture = ImageCapture.Builder()
                .build()

            // Select the back camera as the default camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind any previous use cases before rebinding
                cameraProvider.unbindAll()

                // Bind the use cases to the camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                // Show the PreviewView and hide the camera button
                binding.previewView.visibility = View.VISIBLE

            } catch (exc: Exception) {
                // Handle exceptions
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            externalMediaDirs.firstOrNull(),
            "getCurrentTime()" + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this@CameraActivity),
            object : ImageCapture.OnImageSavedCallback {

                override fun onError(exc: ImageCaptureException) {
                    // Handle the image capture error
                    Toast.makeText(this@CameraActivity, exc.message, Toast.LENGTH_LONG).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Image capture is successful, get the saved image URI
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    imageUri = savedUri
                    // Use the saved URI as needed
                    binding.previewView.visibility = View.GONE
                    binding.cameraImageCaptureBtn.visibility = View.GONE
                    binding.cameraScrollView.visibility = View.GONE
                    binding.cameraImageSendBtn.visibility = View.VISIBLE
                    binding.camaraCapturedImage.apply {
                        visibility = View.VISIBLE
                        setImageURI(savedUri)
                    }
                }
            }
        )
    }


    private fun <T : Serializable?> Intent.getSerializable(key: String, m_class: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            this.getSerializableExtra(key, m_class)!!
        else
            this.getSerializableExtra(key) as T
    }


}