package com.example.dentistver1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp

class MainMenu : AppCompatActivity() {
    private lateinit var cameraButton: ImageButton
    private lateinit var frontCameraButton: Button
    private lateinit var backCameraButton: Button
    private lateinit var surfaceView: SurfaceView
    private var selectedCameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null

    private lateinit var usbManager: UsbManager

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main_menu)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        cameraButton = findViewById(R.id.imageButton)
        frontCameraButton = findViewById(R.id.frontCameraButton)
        backCameraButton = findViewById(R.id.backCameraButton)
        surfaceView = findViewById(R.id.surfaceView)

        cameraButton.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }

        frontCameraButton.setOnClickListener {
            selectCamera(true)
        }

        backCameraButton.setOnClickListener {
            selectCamera(false)
        }
    }

    private fun selectCamera(isFrontCamera: Boolean) {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIds = cameraManager.cameraIdList.filter { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            Log.d("CameraSelection", "Camera ID: $id, Facing: $facing")
            if (isFrontCamera) {
                facing == CameraCharacteristics.LENS_FACING_FRONT
            } else {
                facing == CameraCharacteristics.LENS_FACING_BACK
            }
        }

        if (cameraIds.isEmpty()) {
            val cameraType = if (isFrontCamera) "front" else "back"
            Toast.makeText(this, "No $cameraType camera found", Toast.LENGTH_SHORT).show()
            return
        }

        val cameraNames = cameraIds.map { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            val facingName = if (facing == CameraCharacteristics.LENS_FACING_FRONT) "Front" else "Back"
            "$facingName Camera $id"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Camera")
            .setItems(cameraNames) { _, which ->
                selectedCameraId = cameraIds[which]
                Toast.makeText(this, "Selected Camera: ${cameraNames[which]}", Toast.LENGTH_SHORT).show()
                checkCameraPermissionAndOpenCamera()
            }
            .show()
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            openCamera()
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        val cameraId = selectedCameraId ?: run {
            Toast.makeText(this, "No camera selected", Toast.LENGTH_SHORT).show()
            return
        }
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.d("CameraOpen", "Camera opened successfully: $cameraId")
                    cameraDevice = camera
                    startCameraPreview()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.d("CameraOpen", "Camera disconnected: $cameraId")
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e("CameraOpen", "Error opening camera: $cameraId, Error code: $error")
                    camera.close()
                    cameraDevice = null
                    Toast.makeText(this@MainMenu, "Failed to open camera", Toast.LENGTH_SHORT).show()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            Log.e("CameraOpen", "CameraAccessException: ${e.message}")
        } catch (e: SecurityException) {
            e.printStackTrace()
            Log.e("CameraOpen", "SecurityException: ${e.message}")
        }
    }

    private fun startCameraPreview() {
        val cameraDevice = cameraDevice ?: return
        val surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                createCameraPreviewSession(holder)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraCaptureSession?.close()
                cameraCaptureSession = null
            }
        })
    }

    private fun createCameraPreviewSession(holder: SurfaceHolder) {
        try {
            val cameraDevice = cameraDevice ?: return
            val previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(holder.surface)

            cameraDevice.createCaptureSession(listOf(holder.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    cameraCaptureSession = session
                    previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    session.setRepeatingRequest(previewRequestBuilder.build(), null, null)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Toast.makeText(this@MainMenu, "Failed to configure camera", Toast.LENGTH_SHORT).show()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            Log.e("CameraPreview", "CameraAccessException: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }
}
