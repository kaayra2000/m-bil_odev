package com.example.mobilproje

import GalleryItem
import GalleryItemAdapter
import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilproje.databinding.FragmentGalleryBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.time.LocalDateTime


class GalleryFragment : Fragment() {
    val REQUEST_CODE = 1234
    private var _binding: FragmentGalleryBinding? = null
    val database = FirebaseDatabase.getInstance().reference
    private val binding get() = _binding!!
    private val galleryItems = mutableListOf<GalleryItem>()
    lateinit var galleryItemRecyclerView: RecyclerView
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    lateinit var activity: AppCompatActivity
    lateinit var userName: String
    lateinit var toast: CustomToast
    lateinit var progressDialog: ProgressDialog


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as AppCompatActivity
        activity.supportActionBar?.title = "Gallery"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        progressDialog = ProgressDialog.show(requireContext(), "Uploading", "Please wait...", true)
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        galleryItemRecyclerView = binding.photoGallery
        userName = arguments?.getString("userName")!!
        toast = CustomToast(context)
        val layoutManager = LinearLayoutManager(context)
        galleryItemRecyclerView.layoutManager = layoutManager
        getDataAndSetupAdapter()

       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.save.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(intent, REQUEST_CODE)
        }

    }

    private fun getDataAndSetupAdapter() {
        val myRef = database.child("media").ref
        galleryItems.clear()
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (userSnapshot in dataSnapshot.children) {
                        val user = userSnapshot.getValue(GalleryItem::class.java)
                        user?.let {
                            galleryItems.add(it)
                        }
                    }

                    val adapter = GalleryItemAdapter(galleryItems, this@GalleryFragment,userName)
                    galleryItemRecyclerView.adapter = adapter
                }
                progressDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ListUserFragment", "Verileri alma işlemi başarısız oldu.")
                progressDialog.dismiss()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        galleryItems.clear()
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            val uri = data?.data
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val fileName = uri?.lastPathSegment ?: ""
            val mediaRef = storageRef.child("media/$fileName")
            val progressDialog = ProgressDialog.show(requireContext(), "Uploading", "Please wait...", true)

            mediaRef.putFile(uri!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        val downloadUrl = it.toString()
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH) + 1
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        val today = "$day/$month/$year"
                        println(today)
                        val id = database.child("media").push().key ?: ""
                        val media = GalleryItem(id = id, downloadUrl = downloadUrl, ownerUserName = userName, date = today)
                        database.child("media").child(id).setValue(media)
                        toast.showMessage("Successfully Added",true)
                        progressDialog.dismiss()
                    }
                }
                .addOnFailureListener { exception ->
                    toast.showMessage("Upload failed",false)
                    progressDialog.dismiss()
                }
        }
    }


    override fun onResume() {
        super.onResume()
        activity = context as AppCompatActivity
        activity.supportActionBar?.title = "Gallery"
        galleryItems.clear()
    }



}