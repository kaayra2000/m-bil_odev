package com.example.mobilproje

import GalleryItem
import GalleryItemAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilproje.databinding.FragmentGalleryBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    val database = FirebaseDatabase.getInstance().reference
    private val binding get() = _binding!!
    private val galleryItems = mutableListOf<GalleryItem>()
    lateinit var galleryItemRecyclerView: RecyclerView
    lateinit var userName: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        galleryItemRecyclerView = binding.photoGallery
        userName = arguments?.getString("userName")!!
        val layoutManager = LinearLayoutManager(context)
        galleryItemRecyclerView.layoutManager = layoutManager
        galleryItems.add(GalleryItem("dsadas ","asdads","",""))
        val adapter = GalleryItemAdapter(galleryItems, this@GalleryFragment,userName)
        galleryItemRecyclerView.adapter = adapter

       return binding.root
    }

    private fun getDataAndSetupAdapter() {
        val myRef = database.child("photos").ref

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
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ListUserFragment", "Verileri alma işlemi başarısız oldu.")
            }
        })
    }

}