package com.example.datingappstudy

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.datingappstudy.auth.UserDataModel
import com.example.datingappstudy.setting.SettingActivity
import com.example.datingappstudy.slider.CardStackAdapter
import com.example.datingappstudy.utils.FirebaseAuthUtils
import com.example.datingappstudy.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction

class MainActivity : AppCompatActivity() {

    lateinit var cardStackAdapter: CardStackAdapter
    lateinit var manager: CardStackLayoutManager

    private val TAG = "MainActivity"
    private val usersDataList = mutableListOf<UserDataModel>()

    private lateinit var currentUserGender : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val setting = findViewById<ImageView>(R.id.settingIcon)
        setting.setOnClickListener {

//            val auth = Firebase.auth
//            auth.signOut()
//
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)

        }


        val cardStackView = findViewById<CardStackView>(R.id.cardStackView)

        var userCursor = 0

        manager = CardStackLayoutManager(baseContext, object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {

            }

            override fun onCardSwiped(direction: Direction?) {

                if(direction == Direction.Right) {
//                    Toast.makeText(this@MainActivity, "right", Toast.LENGTH_SHORT).show()
//                    Log.d(TAG, usersDataList[userCount].uid.toString())

                    userLikeOtherUser(FirebaseAuthUtils.getUid(), usersDataList[userCursor].uid.toString())
                }

                if(direction == Direction.Left) {
//                    Toast.makeText(this@MainActivity, "left", Toast.LENGTH_SHORT).show()
                }

                userCursor = userCursor + 1

                if(userCursor == usersDataList.count()) {
                    getUserDataList(currentUserGender)
                    Toast.makeText(this@MainActivity, "유저 새롭게 받아옵니다", Toast.LENGTH_LONG).show()
                }

            }

            override fun onCardRewound() {

            }

            override fun onCardCanceled() {

            }

            override fun onCardAppeared(view: View?, position: Int) {

            }

            override fun onCardDisappeared(view: View?, position: Int) {

            }

        })


        cardStackAdapter = CardStackAdapter(baseContext, usersDataList)
        cardStackView.layoutManager = manager
        cardStackView.adapter = cardStackAdapter

        getMyUserData()
    }


    private fun getMyUserData(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                Log.d(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                Log.d(TAG, data?.gender.toString())

                currentUserGender = data?.gender.toString()

                getUserDataList(currentUserGender)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        val uid = FirebaseAuthUtils.getUid()
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)

    }

    private fun getUserDataList(currentUserGender : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {

                    val user = dataModel.getValue(UserDataModel::class.java)

                    if(user!!.gender.toString().equals(currentUserGender)) {

                    } else {
                        usersDataList.add(user!!)
                    }
                }

                cardStackAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.addValueEventListener(postListener)

    }

    // 유저의 좋아요를 표시하는 부분
    // 데이터에서 값을 저장해야 하는데, 어떤 값을 저장할까...?
    // 나의 uid, 내가 좋아요한 사람의 uid
    private fun userLikeOtherUser(myUid : String, otherUid : String){

        FirebaseRef.userLikeRef.child(myUid).child(otherUid).setValue("true")

        getOtherUserLikeList(otherUid)

    }

    // 내가 좋아요한 사람이 누구를 좋아요 했는지 알 수 있음
    private fun getOtherUserLikeList(otherUid : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // 여기 리스트안에서 나의 UID가 있는지 확인만 해주면 됨.
                // 내가 좋아요한 사람(민지)의 좋아요 리스트를 불러와서
                // 여기서 내 uid가 있는지 체크만 해주면 됨.
                for (dataModel in dataSnapshot.children) {

                    val likeUserKey = dataModel.key.toString()
                    if(likeUserKey.equals(FirebaseAuthUtils.getUid())){
                        Toast.makeText(this@MainActivity, "매칭 완료", Toast.LENGTH_SHORT).show()
                        createNotificationChannel()
                        sendNotification()
                    }

                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(otherUid).addValueEventListener(postListener)


    }

    //Notification

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Test_Channel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(){
        var builder = NotificationCompat.Builder(this, "Test_Channel")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("매칭완료")
            .setContentText("매칭이 완료되었습니다 저사람도 나를 좋아해요")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(123, builder.build())
        }
    }

}