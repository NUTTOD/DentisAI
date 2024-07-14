package com.example.dentistver1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dentistver1.databinding.ActivityMainMenuBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class MainMenu : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var backgroundHandler: Handler
    private lateinit var backgroundThread: HandlerThread

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 200
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private val cameraResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            cameraResultLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.imageButton.setOnClickListener {
            takePhoto()
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
    }

    override fun onPause() {
        stopBackgroundThread()
        super.onPause()
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun startCamera() {
        // Initialize your USB camera here using the library of your choice
        // For example, using a hypothetical USB camera library:
        // usbCamera = UsbCameraManager.openCamera()
        // usbCamera.setPreviewDisplay(binding.viewFinder)
        // usbCamera.startPreview()
    }

    private fun takePhoto() {
        // Capture photo using the USB camera library
        // For example:
        // val photoFile = File(
        //     outputDirectory,
        //     SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        // )
        // usbCamera.capturePhoto(photoFile)
        // val savedUri = Uri.fromFile(photoFile)
        // val msg = "Photo capture succeeded: $savedUri"
        // Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private val outputDirectory: File by lazy {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }
}
