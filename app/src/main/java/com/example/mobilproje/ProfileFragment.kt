package com.example.mobilproje

import GraduatPerson
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_profile_settings.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// TODO: Rename parameter arguments, choose names that match



class ProfileFragment : Fragment() {
    lateinit var email : String
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    val database = FirebaseDatabase.getInstance().reference
    lateinit var user: GraduatPerson
    private var backPressedTime = Long.MIN_VALUE
    lateinit var sharedPrefs : SharedPreferences
    lateinit var editor : SharedPreferences.Editor


    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()
        val activity = context as AppCompatActivity
        activity.supportActionBar?.title = "Profile"
    }






    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val bundle = bundleOf("email" to email)
                findNavController().navigate(R.id.action_profileSettings_to_profileFragment, bundle)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        email = arguments?.getString("email").toString()
        var toast = CustomToast(context)





        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime < 2000 + backPressedTime) {
                    requireActivity().finish()
                } else {
                    backPressedTime = currentTime
                    toast.showMessage("Press again",true)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.createProfileButton.setOnClickListener {
            val bundle = bundleOf("email" to email)
            findNavController().navigate(R.id.action_profile_to_createProfileFragment,bundle)
        }

        binding.findGradButton.setOnClickListener {
            val bundle = bundleOf("email" to email)
            findNavController().navigate(R.id.action_profile_to_findFragment,bundle)
        }

        binding.exitButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle("Are you sure?")
            alertDialogBuilder
                .setMessage("Do you really want to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    editor.putString("email", "")
                    editor.putString("password", "")
                    editor.putBoolean("loginFlag", false)
                    editor.apply()
                    findNavController().navigate(R.id.action_profileSettings_to_FirstFragment)
                }
                .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        lifecycleScope.launch {
            val progressDialog = ProgressDialog.show(requireContext(), "Uploading", "Please wait...", true)
            val parts = email.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val username = parts[0]
            user = database.child("users").child(username).get().await().getValue(
                GraduatPerson::class.java
            )!!

            updateUser()

            initValues()
            progressDialog.dismiss()
        }


        return binding.root
    }


    override fun onResume() {
        super.onResume()
        email = sharedPrefs.getString("email","").toString()
        val activity = context as AppCompatActivity
        activity.supportActionBar?.title = "Profile"
    }
    private fun updateUser(){
        try{
        initValues()
        }catch(e : Exception){
            e.printStackTrace()
        }

    }
    private fun initValues(){

        if (::user.isInitialized) {
            // user değişkeni atanmış durumda
            binding.gradOption.setText(user.situation?.toString())
            binding.nameText.setText(user.name)
            binding.surNameText.setText(user.surName)
            binding.startDateText.setText(user.startDate)
            binding.endDateText.setText(user.endDate)
            binding.userPhoto.setImageDrawable(context?.let { user.photo?.let { it1 ->
                decodeStringToDrawable(it1, it)
            } })
        } else {
            // user değişkeni henüz atanmamış durumda
            // gerekli işlemler yapılabilir
        }


    }

    fun decodeStringToDrawable(encodedString: String, context: Context): Drawable? {
        try {
            val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            return BitmapDrawable(context.resources, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }




}