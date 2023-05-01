package com.example.mobilproje

import Announcement
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.provider.ContactsContract.DisplayPhoto
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobilproje.databinding.FragmentAddAnnouncementBinding
import com.example.mobilproje.databinding.FragmentProfileSettingsBinding
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddAnnouncement : Fragment() {

    val database = FirebaseDatabase.getInstance().reference
    private var _binding: FragmentAddAnnouncementBinding? = null
    private val binding get() = _binding!!
    private var pickImage = 100
    private var imageBitmap: Bitmap? = null
    private var imageUri: Uri? = null
    private var photo: String? = ""
    lateinit var toast: CustomToast

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = context as AppCompatActivity
        activity.supportActionBar?.title = "Add Announcement"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAnnouncementBinding.inflate(inflater, container, false)
        toast = CustomToast(context)

        binding.dateText.setOnClickListener {
            initDatePicker(binding.dateText)
            binding.dateText.setError(null)
        }


        binding.userPhoto.setOnClickListener {
            selectPhoto()
        }

        binding.saveButton.setOnClickListener {
            if (controlFields()) {
                AlertDialog.Builder(context)
                    .setMessage("Are you sure you want to save this announcement?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        addAnnouncement()
                        toast.showMessage("Successfully added", true)
                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }

        return binding.root
    }


    private fun addAnnouncement(){
        val message = binding.messageText.text.toString()
        val date = binding.dateText.text.toString()
        if (imageUri != null)
            photo = convertBitmap(imageUri!!)
        else
            photo = bitmapToString(imageBitmap!!)
        val title = binding.titleText.text.toString()
        val ann = Announcement(date = date, photo = photo!!, message = message, title = title)
        database.child("announcement").push().setValue(ann)
    }

    private fun controlFields(): Boolean{
        var flag = true
        if (binding.titleText.text.toString().length < 3) {
            binding.titleText.error = "Title too short"
            flag = false
        }

        if (binding.titleText.text.toString().length > 25) {
            binding.titleText.error = "Title is too long"
            flag = false
        }

        if (binding.messageText.text.toString().length < 10) {
            binding.messageText.error = "Message too short"
            flag = false
        }

        if (binding.dateText.text.toString().isEmpty()) {
            binding.dateText.error = "Date can't be empty"
            flag = false
        }

        if (imageUri == null) {
            if(imageBitmap == null){
                toast.showMessage("Please select a photo",false)
                flag = false
            }

        }
        return flag
    }



    private fun initDatePicker(editText: TextView){
        val c = Calendar.getInstance()

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = activity?.let {
            DatePickerDialog(
                it,
                { view, year, monthOfYear, dayOfMonth ->
                    val dat = (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    editText.text = (dat)
                },
                year,
                month,
                day
            )
        }

        datePickerDialog?.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                pickImage -> {
                    imageUri = data?.data
                    if (imageUri != null) {
                        binding.userPhoto.setImageURI(imageUri)
                    } else {
                        imageUri = null
                        val extras = data?.extras
                        imageBitmap = extras?.get("data") as Bitmap
                        binding.userPhoto.setImageBitmap(imageBitmap)
                    }
                }
            }
        }
    }

    fun convertBitmap(imageUri: Uri) : String? {
        val inputStream = context?.contentResolver?.openInputStream(imageUri)
        val buffer = ByteArrayOutputStream()

        val bufferSize = 1024
        val bufferArray = ByteArray(bufferSize)

        var len = 0
        while (inputStream?.read(bufferArray, 0, bufferSize).also { len = it!! } != -1) {
            buffer.write(bufferArray, 0, len)
        }

        return Base64.encodeToString(buffer.toByteArray(), Base64.DEFAULT)


    }

    fun bitmapToString(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    private fun selectPhoto(){
        val options = arrayOf<CharSequence>("Galerry", "Camera", "Delete Photo")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Photo")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Galerry" -> {
                    val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                    startActivityForResult(gallery, pickImage)
                }
                options[item] == "Camera" -> {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, pickImage)
                }
                options[item] == "Delete Photo" -> {
                    binding.userPhoto.setImageBitmap(null)
                    imageBitmap = null
                    imageUri = null
                }
            }
        }
        builder.show()
    }

}