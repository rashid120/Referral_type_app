package com.example.f

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.f.model.RegisterModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var nameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var registerBtn: AppCompatButton

    private var point = "0"
    private var referUsername: String? = null
    private var auth = FirebaseAuth.getInstance()

    private val auth1 = FirebaseAuth.getInstance().currentUser?.uid

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginText: TextView = findViewById(R.id.loginTextView)
        loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            finish()
        }

        dynamicLinkReceiver()
        checkLogin()

        nameEt = findViewById(R.id.usernameET)
        emailEt = findViewById(R.id.userEmailET)
        passwordEt = findViewById(R.id.userPasswordET)
        registerBtn = findViewById(R.id.registerBtn)

        registerBtn.setOnClickListener {

            if (emailEt.text.isEmpty() || passwordEt.text.isEmpty() || nameEt.text.isEmpty()){

                return@setOnClickListener
            }else{

                createAccount(emailEt.text.toString(), passwordEt.text.toString(), nameEt.text.toString())
            }
        }

    }

    private fun createAccount(email: String, password: String, name: String){

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){

                    userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

                    Toast.makeText(this, "Register successful", Toast.LENGTH_SHORT).show()

                    if (referUsername != null)
                        uploadData(name, email, password, point, referUsername!!)
                    else
                        uploadData(name, email, password, point, "null")

                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                else{

                    Toast.makeText(this, "Something wrong", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Upload user data in firebase
    private fun uploadData(name: String, email: String, password: String, point: String, referUsername: String){

        val data = RegisterModel(name, email, password, point, referUsername)

//        val documentName = UUID.randomUUID().toString()

        Firebase.firestore.collection("EarningApp")

            .document(userId)
            .set(data)
            .addOnSuccessListener {

                Toast.makeText(this, "Uploaded In Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {

                Toast.makeText(this, "Not Uploaded In Firebase", Toast.LENGTH_SHORT).show()
            }

    }

    private fun dynamicLinkReceiver(){

        Firebase.dynamicLinks.getDynamicLink(intent)
            .addOnSuccessListener { pendingDynamicLinkData ->

                var deepLink: Uri? = null

                if (pendingDynamicLinkData != null){

                    deepLink = pendingDynamicLinkData.link
                }
                if (deepLink != null){

                    referUsername = deepLink.getQueryParameter("username").toString()
                    point = deepLink.getQueryParameter("referCode").toString()
                }
            }
    }

    private fun checkLogin(){

        if (auth1 != null){

            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

}