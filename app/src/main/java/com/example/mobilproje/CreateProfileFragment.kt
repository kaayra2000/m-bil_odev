package com.example.mobilproje

import StudentProfile
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import com.example.mobilproje.databinding.FragmentCreateProfileBinding
import com.example.mobilproje.databinding.FragmentRegisterBinding
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_create_profile.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream


class CreateProfileFragment : Fragment() {

    private var _binding: FragmentCreateProfileBinding? = null
    private val binding get() = _binding!!
    val database = FirebaseDatabase.getInstance().reference
    lateinit var sharedPrefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    lateinit var toast : CustomToast
    lateinit var workEditText: EditText
    lateinit var imageView: ImageView
    lateinit var socialMediaEditText: EditText
    lateinit var nameEditText: EditText
    lateinit var surNameEditText: EditText
    lateinit var emailEditText: EditText
    lateinit var phoneNumberEditText: EditText
    lateinit var registerButton: Button
    lateinit var deleteButton: Button
    lateinit var gradOption: Spinner
    lateinit var username: String
    lateinit var studentProfile: StudentProfile
    private var imageBitmap: Bitmap? = null
    private var photo: String? = ""
    private var imageUri: Uri? = null
    private var pickImage = 100
    lateinit var email: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateProfileBinding.inflate(inflater, container, false)
        initViewFields()
        lifecycleScope.launch {
            val dataSnapshot = database.child("profilestudent").child(username).get().await()
            if(dataSnapshot.exists()){
                studentProfile = dataSnapshot.getValue(StudentProfile::class.java)!!
            }
            else{
                studentProfile = StudentProfile()
            }
            setEditTextsValues()
        }

        imageView.setOnClickListener {
            selectPhoto()
        }

        registerButton.setOnClickListener {
            putStudentProfileDatabase()
        }

        deleteButton.setOnClickListener {
            deleteUser()
        }

        return binding.root
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
    private fun initViewFields(){
        workEditText = binding.workText
        imageView = binding.userPhoto
        socialMediaEditText = binding.socialMedia
        registerButton = binding.registerButton
        nameEditText = binding.nameText
        surNameEditText = binding.surNameText
        emailEditText = binding.emailText
        deleteButton = binding.deleteButton
        gradOption = binding.gradOption
        phoneNumberEditText = binding.phoneNumberText
        toast = CustomToast(context)
        val bundle = arguments
        email = bundle?.getString("email")!!
        val parts = email.split("@".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        username = parts[0]
    }
    private fun controlAllFields():Boolean{
        var flag = true
        if (nameEditText.text.toString().length < 5){
            flag = false
            nameEditText.setError("Wrong Name")
        }

        if (surNameEditText.text.toString().length < 5){
            flag = false
            surNameEditText.setError("Wrong Surame")
        }

        if (phoneNumberEditText.text.toString().length!=10 ||
                phoneNumberEditText.text.toString()[0] != '5'){
            flag = false
            phoneNumberEditText.setError("Wrong Phone Number")
        }

        val pattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()

        if (emailEditText.text.toString().matches(pattern)){
            flag = false
            emailEditText.setError("Wrong Email")
        }

        if (workEditText.text.toString().length < 3){
            flag = false
            workEditText.setError("Wrong Work Info")
        }

        return flag
    }

    private fun createStudentProfile(){
        photo = imageUri?.let { it1 -> convertBitmap(it1) }
        if (imageUri == null)
            if (imageBitmap != null)
                photo = imageBitmap?.let { it-> bitmapToString(it) }
        studentProfile = StudentProfile(
            name = nameEditText.text.toString(),
            surName = surNameText.text.toString(),
            email = emailEditText.text.toString(),
            phoneNumber = phoneNumberEditText.text.toString(),
            workInfo = workEditText.text.toString(),
            socialMedia = socialMediaEditText.text.toString(),
            photo = photo,
            situation = situation.valueOf(binding.gradOption.selectedItem.toString()),
            userName = username
        )
    }

    fun bitmapToString(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
   private fun convertBitmap(imageUri: Uri) : String? {
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

    private fun convertStringToBitmap(string: String?): Bitmap? {
        if(string != null){
            val decodedString = Base64.decode(string, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
        return null
    }

    private fun deleteUser(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Are you sure to delete profile?")
        builder.setPositiveButton("Yes") { _, _ ->
            lifecycleScope.launch {
                val parts = email.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val username = parts[0]
                database.child("profilestudent").child(username)
                    .removeValue()
                toast.showMessage("Successfully Deleted",true)
                studentProfile = StudentProfile()
                setEditTextsValues()

            }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
            toast.showMessage("Cancelled",false)
        }
        val dialog = builder.create()
        dialog.show()

    }
    private fun putStudentProfileDatabase(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Are you sure?")
        builder.setPositiveButton("Yes") { _, _ ->
            if(controlAllFields()){
                lifecycleScope.launch {
                    createStudentProfile()
                    database.child("profilestudent").child(username)
                        .setValue(studentProfile)
                    toast.showMessage("Successfully",true)

                }

            }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
            toast.showMessage("Cancelled",false)
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun setEditTextsValues(){
        if (::studentProfile.isInitialized) {
            nameEditText.setText(studentProfile.name)
            surNameEditText.setText(studentProfile.surName)
            emailEditText.setText(studentProfile.email)
            phoneNumberEditText.setText(studentProfile.phoneNumber)
            workEditText.setText(studentProfile.workInfo)
            socialMediaEditText.setText(studentProfile.socialMedia)
            imageView.setImageBitmap(convertStringToBitmap(studentProfile.photo))
        } else {
            toast.showMessage("Profile Not Found",false)
             }


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