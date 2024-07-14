package com.example.dentistver1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dentistver1.databinding.ActivityMainMenuBinding
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera
import com.serenegiant.usb.UVCCameraHandler
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class MainMenu : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var backgroundHandler: Handler
    private lateinit var backgroundThread: HandlerThread
    private lateinit var usbMonitor: USBMonitor
    private var uvcCameraHandler: UVCCameraHandler? = null

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

        usbMonitor = USBMonitor(this, onDeviceConnectListener)

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
        usbMonitor.register()
    }

    override fun onPause() {
        stopBackgroundThread()
        usbMonitor.unregister()
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
        // USB camera initialization will be handled in the onDeviceConnectListener
    }

    private fun takePhoto() {
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )
        uvcCameraHandler?.captureStill(photoFile.absolutePath) { result ->
            val savedUri = Uri.fromFile(photoFile)
            val msg = "Photo capture succeeded: $savedUri"
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private val onDeviceConnectListener = object : USBMonitor.OnDeviceConnectListener {
        override fun onAttach(device: UsbDevice?) {
            usbMonitor.requestPermission(device)
        }

        override fun onDettach(device: UsbDevice?) {
            uvcCameraHandler?.close()
        }

        override fun onConnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?, createNew: Boolean) {
            uvcCameraHandler = UVCCameraHandler.createHandler(
                this@MainMenu,
                binding.viewFinder,
                1,
                UVCCamera.DEFAULT_PREVIEW_WIDTH,
                UVCCamera.DEFAULT_PREVIEW_HEIGHT,
                UVCCamera.FRAME_FORMAT_MJPEG
            )
            uvcCameraHandler?.open(ctrlBlock)
            uvcCameraHandler?.startPreview()
        }

        override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            uvcCameraHandler?.close()
        }

        override fun onCancel(device: UsbDevice?) {
            // Handle cancel event if needed
        }
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
