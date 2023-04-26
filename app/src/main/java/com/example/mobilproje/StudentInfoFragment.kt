package com.example.mobilproje

import StudentProfile
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.mobilproje.databinding.FragmentStudentInfoBinding
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_student_info.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


private var _binding: FragmentStudentInfoBinding? = null
private val binding get() = _binding!!
lateinit var studentProfile: StudentProfile
lateinit var toast : CustomToast
val database = FirebaseDatabase.getInstance().reference
private lateinit var userName: String
class StudentInfoFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentStudentInfoBinding.inflate(inflater, container, false)
        toast = CustomToast(context)
        userName = arguments?.getString("userName").toString()
        lifecycleScope.launch {
            val dataSnapshot = database.child("profilestudent").child(userName).get().await()
            if(dataSnapshot.exists()){
                studentProfile = dataSnapshot.getValue(StudentProfile::class.java)!!
            }
            else{
                studentProfile = StudentProfile()
            }
            setTextViewValues()
        }

        binding.emailText.setOnClickListener {
            goEmailIntent(binding.emailText.text.toString())
        }

        binding.phoneNumberText.setOnClickListener {
            goCallIntent(binding.phoneNumberText.text.toString())

        }
        return binding.root
    }


    private fun setTextViewValues(){
        if (::studentProfile.isInitialized) {
            binding.nameText.setText(studentProfile.name)
            binding.surNameText.setText(studentProfile.surName)
            binding.emailText.setText(studentProfile.email)
            binding.phoneNumberText.setText(studentProfile.phoneNumber)
            binding.workText.setText(studentProfile.workInfo)
            binding.socialMedia.setText(studentProfile.socialMedia)
            binding.userPhoto.setImageBitmap(convertStringToBitmap(studentProfile.photo))
        } else {
            toast.showMessage("Profile Not Found",false)
        }

    }
    private fun goEmailIntent(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Grad. App")
        startActivity(Intent.createChooser(intent, "Choose an email app:"))
    }

    private fun goCallIntent(phone: String){
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }
        startActivity(intent)
    }


    private fun convertStringToBitmap(string: String?): Bitmap? {
        if(string != null){
            val decodedString = Base64.decode(string, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
        return null
    }


}