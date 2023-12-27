package com.example.f

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var loginBtn: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val registerText: TextView = findViewById(R.id.registerTextView)
        registerText.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            finish()
        }

        emailEt = findViewById(R.id.userEmailETL)
        passwordEt = findViewById(R.id.userPasswordETL)
        loginBtn = findViewById(R.id.loginBtn)

        loginBtn.setOnClickListener {

            loginAccount(emailEt.text.toString(), passwordEt.text.toString())
        }
    }

    private fun loginAccount(email: String, password: String){

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener {

                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
            }
    }
}