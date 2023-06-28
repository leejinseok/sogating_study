package com.example.datingappstudy

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.datingappstudy.auth.IntroActivity
import com.example.datingappstudy.utils.FirebaseAuthUtils

class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashActivity"

//    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

//        val uid = auth.currentUser?.uid.toString()
        val uid = FirebaseAuthUtils.getUid()

        if (uid == "null") {

            Handler().postDelayed({
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }, 3000)

        } else {

            Handler().postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }, 3000)

        }



    }

}