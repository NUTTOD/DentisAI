package com.example.dentistver1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
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
    private var selectedCameraId: String? = null

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

        cameraButton = findViewById(R.id.imageButton)
        frontCameraButton = findViewById(R.id.frontCameraButton)
        backCameraButton = findViewById(R.id.backCameraButton)

        cameraButton.setOnClickListener {
            if (selectedCameraId == null) {
                Toast.makeText(this, "Please select a camera first", Toast.LENGTH_SHORT).show()
            } else {
                checkCameraPermissionAndOpenCamera()
            }
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
            if (isFrontCamera) {
                facing == CameraCharacteristics.LENS_FACING_FRONT
            } else {
                facing == CameraCharacteristics.LENS_FACING_BACK || facing == CameraCharacteristics.LENS_FACING_EXTERNAL
            }
        }

        if (cameraIds.isEmpty()) {
            Toast.makeText(this, "No camera found", Toast.LENGTH_SHORT).show()
            return
        }

        val cameraNames = cameraIds.map { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            when (facing) {
                CameraCharacteristics.LENS_FACING_FRONT -> "Front Camera"
                CameraCharacteristics.LENS_FACING_BACK -> "Back Camera"
                CameraCharacteristics.LENS_FACING_EXTERNAL -> "External Camera"
                else -> "Unknown Camera"
            }
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

    private fun openCamera() {
        val cameraId = selectedCameraId ?: run {
            Toast.makeText(this, "No camera selected", Toast.LENGTH_SHORT).show()
            return
        }
        // Implement camera opening using Camera2 API with the selected cameraId
        // This is a placeholder Toast to show the selected cameraId
        Toast.makeText(this, "Selected Camera ID: $cameraId", Toast.LENGTH_SHORT).show()
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
