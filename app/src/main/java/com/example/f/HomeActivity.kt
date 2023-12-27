package com.example.f

import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.f.model.RegisterModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.iosParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {

    private var shortLink = ""
    private var auth = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var pointTextView: TextView
    private lateinit var referalUsers: TextView

    private lateinit var progress: ProgressDialog

    private var nameArr = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val shareT: TextView = findViewById(R.id.shareTextView)
        pointTextView = findViewById(R.id.pointTV)
        referalUsers = findViewById(R.id.referUsers)

        val toolbar: Toolbar = findViewById(R.id.toolbar)

        toolbar.title = "Earn money"
        toolbar.subtitle = "Earning app"
        toolbar.setOnMenuItemClickListener {

            when(it.itemId){
                R.id.logOut -> {

                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                R.id.refresh ->{

                    updatePoints()
                    progress = ProgressDialog(this)
                    progress.show()
                }
            }
            true
        }
        getData()

        //Explicitly allowlist dynamic link URL (Android 13)
        val intentFilter = IntentFilter(Intent.ACTION_VIEW)
        intentFilter.addDataScheme("https")
        intentFilter.addDataAuthority("google.com", null)

        registerReceiver(dynamicLinkReceiver, intentFilter)

        Firebase.dynamicLinks.shortLinkAsync {

            link = Uri.parse("https://www.google.com/referral?username=$auth&referCode=100")
            domainUriPrefix = "https://earningappf.page.link"

            androidParameters {  }
            iosParameters("com.example.ios"){}
        }.addOnSuccessListener {

            shortLink = it.shortLink.toString()
        }.addOnFailureListener {

            Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
        }


        shareT.setOnClickListener {

            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_SUBJECT, "App link")
            intent.putExtra(Intent.EXTRA_TEXT, shortLink)

            startActivity(Intent.createChooser(intent, "Dynamic link app"))
        }
    }

    private val dynamicLinkReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {

            var link: String? = null
            if (p1?.data != null){

                link = p1.data.toString()
            }
            Toast.makeText(this@HomeActivity, link.toString() + "From Broadcast HomePage", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getData(){

        val db = Firebase.firestore
        db.collection("EarningApp")
            .document(auth.toString())
            .get()
            .addOnSuccessListener { result ->
                val data = result.data

                val point = data?.get("point").toString()
                pointTextView.text = point
            }
    }

    private fun updatePoints(){

        Firebase.firestore.collection("EarningApp")
            .whereEqualTo("userName", auth)
            .get()
            .addOnSuccessListener { result ->

                if (!result.isEmpty){

                    var count = 1

                    for (i in result.documents){

                        nameArr.add(i.get("name").toString())
                        count++
                    }

                    if (count != 1) {

                        val point = (100 * count).toString()

                        Firebase.firestore.collection("EarningApp")
                            .document(auth.toString())
                            .update("point", point)
                            .addOnSuccessListener {

                                progress.dismiss()
                                val intent = Intent(this, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                            }

                    }
                    if (nameArr.isNotEmpty())

                        referalUsers.text = nameArr.toString()
                    else
                        referalUsers.text = "No user available"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }
}