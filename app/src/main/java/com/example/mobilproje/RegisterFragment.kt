package com.example.mobilproje

import GraduatPerson
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.telephony.PhoneNumberUtils
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import situation
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


val PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).{8,}$"
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    val database = FirebaseDatabase.getInstance().reference
    private val binding get() = _binding!!
    private var pickImage = 100
    private lateinit var auth: FirebaseAuth
    lateinit var toast : CustomToast
    lateinit var sharedPrefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    lateinit var imageView: ImageView
    lateinit var person: GraduatPerson
    private var imageUri: Uri? = null
    lateinit var passwordNameEditText: EditText
    lateinit var nameEditText: EditText
    lateinit var surNameEditText: EditText
    lateinit var eMailEditText: EditText
    lateinit var phoneNumberEditText: EditText
    lateinit var startDateEditText: EditText
    lateinit var endDateEditText: EditText
    lateinit var rePasswordEditText: EditText

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        imageView = binding.userPhoto
        toast = CustomToast(context)
        auth = FirebaseAuth.getInstance()
        binding.startDateText.setOnClickListener {
            initDatePicker(binding.startDateText)
            binding.startDateText.setError(null)
        }

        initEditTexts()

        binding.passwordText.setText(sharedPrefs.getString("password", "").toString())
        binding.emailText.setText(sharedPrefs.getString("email", "").toString())
        binding.endDateText.setOnClickListener {
            initDatePicker(binding.endDateText)
            binding.endDateText.setError(null)
        }

        imageView.setOnClickListener {
            selectPhoto()
        }
        val getValue = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {

            }
        }
        database.addValueEventListener(getValue)
        database.addListenerForSingleValueEvent(getValue)


        binding.registerButton.setOnClickListener {
            val email = binding.emailText.text.toString()
            val name = binding.nameText.text.toString()
            val surName = binding.surNameText.text.toString()
            val phoneNumber = binding.phoneNumberText.text.toString()
            val password = binding.passwordText.text.toString()
            val endDate = binding.endDateText.text.toString()
            val startDate = binding.startDateText.text.toString()
            val rePassword = binding.passwordRetypeText.text.toString()
            val photo = imageUri?.let { it1 -> convertBitmap(it1) }

            editor.putString("password", password)
            editor.apply()

            val getError = controlAllFields(email, name,surName, phoneNumber, startDate
                ,endDate, password, situation.valueOf(binding.gradOption.selectedItem.toString()),
                photo, rePassword)
            if (!getError){
                return@setOnClickListener
            }
            database.child("users").addListenerForSingleValueEvent(
                object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var flag = true
                        var message = ""
                        for (i in snapshot.children) {
                            val tmpEmail = i.child("email").getValue().toString()
                            val tmpPhoneNumber = i.child("phoneNumber").getValue().toString()
                            if (tmpEmail == email || tmpPhoneNumber == phoneNumber) {
                                if (tmpEmail == email) message += "Email is exists\n"
                                if (tmpPhoneNumber == phoneNumber) message += "Phone Number is exists\n"
                                toast.showMessage(message,false)
                                flag = false
                                break
                            }
                        }
                        if(flag){
                            authUsers(person.email,person.password)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }


                }
            )




        }

        binding.setLifecycleOwner(this)

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                pickImage -> {
                    val imageUri = data?.data
                    if (imageUri != null) {
                        imageView.setImageURI(imageUri)
                    } else {
                        val extras = data?.extras
                        val imageBitmap = extras?.get("data") as Bitmap
                        imageView.setImageBitmap(imageBitmap)
                    }
                }
            }
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
                    imageView.setImageBitmap(null)
                }
            }
        }
        builder.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun controlAllFields(
        email: String?, name: String?, surName: String?,
        phoneNumber: String, startDate: String?, endDate: String?,
        password: String, situation: situation, photo: String?, rePassword: String
    ): Boolean {
        var returnVal = true




        if (name?.length!! <4){
            nameEditText.setError("Incorrect Name")
            returnVal = false
        }


        if (surName?.length!! <2){
            surNameEditText.setError("Incorrect Surname")
            returnVal = false
        }

        if (email?.length!!<5 || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.toString()).matches()){
            eMailEditText.setError("Incorrect Mail")
            returnVal = false
        }

        if (!PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)){
            phoneNumberEditText.setError("Incorrect Phone Number")
            returnVal = false
        }
        if(startDate?.length!!>1 && endDate?.length!!>1){
            val sdf = SimpleDateFormat("dd-MM-yyyy")
            val firstDate: Date = sdf.parse(startDate)
            val secondDate: Date = sdf.parse(endDate)
            person = GraduatPerson(name,surName,email,phoneNumber, startDate, endDate, situation,password,
                photo)
            if (firstDate.after(secondDate) /*|| secondDate.after(today)*/){
                startDateEditText.setError("Incorrect Date")
                endDateEditText.setError("Incorrect Date")
                returnVal = false
            }
        }
        else{
            returnVal = false
            if(startDate?.length!!<3){
                startDateEditText.setError("Date is null")
            }
            if(startDate?.length!!<3){
                endDateEditText.setError("Date is null")
            }
        }

        if (!PASSWORD_REGEX.toRegex().matches(password)){
            returnVal = false
            passwordNameEditText.setError("Password must contain at least one " +
                    "uppercase letter, one lowercase letter and one number")
        }
        if(!password.equals(rePassword))
            rePasswordEditText.setError("Passwords are not match")

        return returnVal
    }

    private fun authUsers(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    auth.currentUser!!.sendEmailVerification()
                        .addOnSuccessListener {
                            toast.showMessage("Email sended",true)
                            updateUI(auth.currentUser)
                        }.addOnFailureListener{
                            toast.showMessage("Email cannot send",false)

                        }

                } else {
                    updateUI(null)
                    toast.showMessage("Email already used",false)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val email = person.email
            val parts = email.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val username = parts[0]
            database.child("users").child(username).setValue(person)
            toast.showMessage("Successfully Registered",true)
            findNavController().popBackStack()
        }
    }
    private fun initDatePicker(editText: EditText){
        val c = Calendar.getInstance()

        // on below line we are getting
        // our day, month and year.
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // on below line we are creating a
        // variable for date picker dialog.
        val datePickerDialog = activity?.let {
            DatePickerDialog(
                // on below line we are passing context.
                it,
                { view, year, monthOfYear, dayOfMonth ->
                    // on below line we are setting
                    // date to our edit text.
                    val dat = (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    editText.setText(dat)
                },
                // on below line we are passing year, month
                // and day for the selected date in our date picker.
                year,
                month,
                day
            )
        }
        // at last we are calling show
        // to display our date picker dialog.
        datePickerDialog?.show()
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

    private fun initEditTexts(){
        passwordNameEditText = binding.passwordText
        nameEditText = binding.nameText
        surNameEditText = binding.surNameText
        eMailEditText = binding.emailText
        phoneNumberEditText = binding.phoneNumberText
        startDateEditText = binding.startDateText
        endDateEditText = binding.endDateText
        rePasswordEditText = binding.passwordRetypeText
    }


}