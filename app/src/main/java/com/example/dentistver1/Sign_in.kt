package com.example.dentistver1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // สร้างตัวแปรสำหรับ EditText และ Button
        val emailEditText: EditText = findViewById(R.id.etEmail)
        val passwordEditText: EditText = findViewById(R.id.etPassword)
        val signInButton: Button = findViewById(R.id.btnSignIn)

        // เมื่อคลิกปุ่ม Sign In
        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // ตรวจสอบข้อมูลที่ป้อนเข้าไป
            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (email == "user@example.com" && password == "password123") {
                    Toast.makeText(this, "Sign In successful", Toast.LENGTH_SHORT).show()
                    // เมื่อสำเร็จแล้วเปลี่ยนไปหน้า MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
