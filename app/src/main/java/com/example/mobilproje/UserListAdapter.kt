package com.example.mobilproje

import StudentProfile
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.user_item.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class UserListAdapter(private val userList: List<StudentProfile>, private val fragment: Fragment) :

    RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {
    lateinit var sharedPrefs : SharedPreferences
    val database = FirebaseDatabase.getInstance().reference
    lateinit var editor : SharedPreferences.Editor

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        sharedPrefs = parent.context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()
        return UserListViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class UserListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(user: StudentProfile) = GlobalScope.launch {
            val dataSnapshot = database.child("users").child(user.userName).get().await()
            if (dataSnapshot.exists()) {
                withContext(Dispatchers.Main) {
                    itemView.imageView.setImageBitmap(user.photo?.let { convertStringToBitmap(it) })
                    if(itemView.imageView != null) {
                        itemView.imageView.background = null
                    }
                    itemView.nameText.text = "Name: " + user.name
                    itemView.currClassText.text = "Surname: " + user.surName
                    itemView.durationText.text = "Work Info: " +  user.workInfo
                    itemView.situationText.text = "Situation: " + user.situation!!.situation
                    val bundle = bundleOf("userName" to user.userName)
                    itemView.imageView.setOnClickListener {
                        fragment.findNavController().navigate(R.id.action_listStudentFragment_to_studentInfoFragment, bundle)
                    }
                }
            } else {
                itemView.setOnClickListener {
                    GlobalScope.launch {
                        withContext(Dispatchers.Main) {
                            CustomToast(itemView.context).apply {
                                showMessage("User Cannot Found", isSuccess = false)
                            }
                        }
                    }
                }
            }

        }

    }




    fun convertStringToBitmap(encodedString: String): Bitmap? {
        return try {
            val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message?.let { Log.d("Error", it) }
            null
        }
    }
}
