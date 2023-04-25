package com.example.mobilproje

import GraduatPerson
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    lateinit var sharedPrefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    lateinit var email: String
    private lateinit var auth: FirebaseAuth
    lateinit var toast: CustomToast
    val database = FirebaseDatabase.getInstance().reference
    lateinit var password: String
    var loginFlag = true
    private val binding get() = _binding!!


    override fun onResume() {
        super.onResume()
        changeEnabledStop()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        if(sharedPrefs.getBoolean("loginFlag", false))
            changeEnabledLoading()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        toast = CustomToast(context)
        binding.loading.visibility = View.VISIBLE
        loginFlag = sharedPrefs.getBoolean("loginFlag", false)
        binding.userNameText.setText(sharedPrefs.getString("email", "").toString())
        binding.passwordText.setText(sharedPrefs.getString("password", "").toString())
        email = binding.userNameText.text.toString()
        password = binding.passwordText.text.toString()
        var username = ""
        if(email.length > 1){
            val parts = email.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            if(parts[0].length > 0)
                username = parts[0]
        }


        binding.registerButton.setOnClickListener {
            editor.putString("email", binding.userNameText.text.toString())
            editor.putString("password", binding.passwordText.text.toString())
            editor.putBoolean("loginFlag", false)
            editor.apply()
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        database.child("users").child(username).addListenerForSingleValueEvent(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        if(snapshot.child("password").getValue().toString().equals(sharedPrefs.getString("password", "").toString())
                            && loginFlag){
                            lifecycleScope.launch {
                                binding.userNameText.setText(sharedPrefs.getString("email", "").toString())
                                binding.passwordText.setText(sharedPrefs.getString("password", "").toString())
                                email = binding.userNameText.text.toString()
                                password = binding.passwordText.text.toString()
                                checkIfUserExists(email,password)
                            }

                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }


            }
        )

        binding.forgotPasswordText.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Forgot Password")
            builder.setMessage("Please enter your email address to reset your password:")

            val input = EditText(requireContext())
            input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            builder.setView(input)

            builder.setPositiveButton("Reset Password") { _, _ ->
                val email = input.text.toString()
                if (email.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter your email address", Toast.LENGTH_SHORT).show()
                } else {
                    auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            toast.showMessage("Password reset email sent to $email", true)
                        } else {
                            toast.showMessage("Failed to send password reset email", false)
                        }
                    }
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

            val dialog = builder.create()
            dialog.show()

            input.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
        binding.loginButton.setOnClickListener{


            email = binding.userNameText.text.toString()
            password = binding.passwordText.text.toString()

            lifecycleScope.launch {
                checkIfUserExists(email, password)
            }
        }

    }

    private fun changeEnabledLoading(){
        binding.loading.visibility = View.VISIBLE
        binding.registerButton.isEnabled = false
        binding.passwordText.isEnabled = false
        binding.userNameText.isEnabled = false
        binding.forgotPasswordText.isEnabled = false
        binding.loginButton.isEnabled = false
    }

    private fun changeEnabledStop(){
        binding.passwordText.isEnabled = true
        binding.userNameText.isEnabled = true
        binding.forgotPasswordText.isEnabled = true
        binding.loading.visibility = View.GONE
        binding.registerButton.isEnabled = true
        binding.loginButton.isEnabled = true
    }
    private fun checkIfUserExists(email: String, password: String) {
        changeEnabledLoading()
        if (email.length > 6 && password.length >6){
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if(user!!.isEmailVerified){
                        try {
                            toast.showMessage("Successfully Logged In", true)
                            editor.putString("password", password)
                            editor.putString("email",email)
                            editor.putBoolean("loginFlag",true)
                            editor.apply()
                            val bundle = bundleOf("email" to email)
                            findNavController().navigate(R.id.action_FirstFragment_to_profileSettings,bundle)
                        }catch (e: java.lang.Exception){
                            toast.showMessage("Error",false)
                        }

                    }
                    else{
                        changeEnabledStop()
                        user.sendEmailVerification()
                        toast.showMessage("User not verified yet",false)
                    }
                }
                else{
                    changeEnabledStop()
                    toast.showMessage("Email and password not match",false)
                }
            }}
        else{
            changeEnabledStop()
            toast.showMessage("Short mail or password",false)
        }
    }


}