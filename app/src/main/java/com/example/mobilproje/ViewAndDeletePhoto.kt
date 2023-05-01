package com.example.mobilproje

import android.app.ProgressDialog
import android.content.Context
import android.widget.MediaController
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mobilproje.databinding.FragmentGalleryBinding
import com.example.mobilproje.databinding.FragmentViewAndDeletePhotoBinding
import com.google.firebase.database.FirebaseDatabase
import java.util.*


class ViewAndDeletePhoto : Fragment() {
    private var _binding: FragmentViewAndDeletePhotoBinding? = null
    var isOwner = false
    lateinit var url: String
    lateinit var progressDialog: ProgressDialog
    val database = FirebaseDatabase.getInstance().reference
    lateinit var mediaID: String
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = context as AppCompatActivity
        activity.supportActionBar?.title = "Show/Edit Media"
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        progressDialog = ProgressDialog.show(requireContext(), "Uploading", "Please wait...", true)
        _binding = FragmentViewAndDeletePhotoBinding.inflate(inflater, container, false)
        arguments?.getBoolean("isOwner",false)?.let {
            isOwner = it
        }
        arguments?.getString("mediaID","")?.let {
            mediaID = it
        }
        arguments?.getString("url","")?.let {
            url = it
        }
        if(isOwner)
            binding.deleteButton.visibility = View.VISIBLE
        if(getFileTypeFromUrl(url).equals("video"))
            playVideo(url)
        else{
            binding.photoView.visibility = View.VISIBLE
            binding.videoView.visibility = View.GONE
            loadImageViewWithUrl(url, binding.photoView)
        }

        binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to delete this media?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteMedia()
                    CustomToast(context).showMessage("Successfully deleted", true)
                    findNavController().popBackStack()
                }
                .setNegativeButton("No", null)
                .show()
        }

        return binding.root
    }


    private fun playVideo(videoUrl: String) {
        val mediaController = MediaController(requireContext())
        val videoView = binding.videoView
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        videoView.setVideoURI(Uri.parse(videoUrl))
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.setOnVideoSizeChangedListener { _, width, height ->
                setVideoViewSize(width, height)
            }
            progressDialog.dismiss()
            videoView.start()
        }
        videoView.setOnCompletionListener {
            mediaController.show(0)
        }
    }
    private fun setVideoViewSize(videoWidth: Int, videoHeight: Int) {
        val videoView = binding.videoView
        val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
        val screenWidth = requireActivity().windowManager.defaultDisplay.width
        val screenHeight = requireActivity().windowManager.defaultDisplay.height
        val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()
        val lp = videoView.layoutParams
        if (videoProportion > screenProportion) {
            lp.width = screenWidth
            lp.height = (screenWidth / videoProportion).toInt()
        } else {
            lp.width = (videoProportion * screenHeight).toInt()
            lp.height = screenHeight
        }
        videoView.layoutParams = lp
    }

    private fun getFileTypeFromUrl(url: String): String {
        return if (url.contains("video")) {
            "video"
        } else {
            "photo"
        }
    }
    private fun loadImageViewWithUrl(url: String, imageView: ImageView) {
        Glide.with(imageView.context)
            .load(url)
            .into(imageView)
        progressDialog.dismiss()

    }

    private fun deleteMedia(){
        database.child("media").child(mediaID).removeValue()
    }



}