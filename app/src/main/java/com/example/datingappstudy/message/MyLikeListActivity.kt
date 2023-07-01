package com.example.datingappstudy.message


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import com.example.datingappstudy.R
import com.example.datingappstudy.auth.UserDataModel
import com.example.datingappstudy.utils.FirebaseAuthUtils
import com.example.datingappstudy.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


// 내가 좋아요한 사람들이 나를 좋아요 한 리스트
// 내가 민지, 현아, 다솜 -> 현아, 다솜
class MyLikeListActivity : AppCompatActivity() {

    private val TAG = "MyLikeListActivity"
    private val uid = FirebaseAuthUtils.getUid()

    private val likeUserListUid = mutableListOf<String>()
    private val likeUserList = mutableListOf<UserDataModel>()

    lateinit var listviewAdapter : ListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_like_list)


        val userListView = findViewById<ListView>(R.id.userListView)

        listviewAdapter = ListViewAdapter(this, likeUserList)
        userListView.adapter = listviewAdapter


        // 내가 좋아요한 사람들
        getMyLikeList()

        // 제가 하고 싶은것은
        // 전체 유저 중에서, 내가 좋아요한 사람들 가져와서
        // 이 사람이 나와 매칭이 되어있는지 확인하는 것!!

        userListView.setOnItemClickListener { parent, view, position, id ->

//            Log.d(TAG, likeUserList[position].uid.toString())
            checkMatching(likeUserList[position].uid.toString())

        }


    }

    private fun checkMatching(otherUid : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                Log.d(TAG, otherUid)
                Log.e(TAG, dataSnapshot.toString())

                if(dataSnapshot.children.count() == 0) {

                    Toast.makeText(this@MyLikeListActivity, "매칭이 되지 않았습니다.", Toast.LENGTH_LONG).show()

                } else {

                    for (dataModel in dataSnapshot.children) {

                        val likeUserKey = dataModel.key.toString()
                        if(likeUserKey.equals(uid)) {
                            Toast.makeText(this@MyLikeListActivity, "매칭이 되었습니다.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@MyLikeListActivity, "매칭이 되지 않았습니다.", Toast.LENGTH_LONG).show()
                        }

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

    private fun getMyLikeList(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {
                    // 내가 좋아요 한 사람들의 uid가  likeUserList에 들어있음
                    likeUserListUid.add(dataModel.key.toString())
                }
                getUserDataList()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(uid).addValueEventListener(postListener)

    }

    private fun getUserDataList(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {

                    val user = dataModel.getValue(UserDataModel::class.java)

                    // 전체 유저중에 내가 좋아요한 사람들의 정보만 add함
                    if(likeUserListUid.contains(user?.uid)) {

                        likeUserList.add(user!!)
                    }

                }
                listviewAdapter.notifyDataSetChanged()
                Log.d(TAG, likeUserList.toString())

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.addValueEventListener(postListener)

    }


}