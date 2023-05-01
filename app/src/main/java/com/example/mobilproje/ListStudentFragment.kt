package com.example.mobilproje

import GraduatPerson
import StudentProfile
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilproje.databinding.FragmentListStudentBinding
import com.example.mobilproje.databinding.FragmentLoginBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*


class ListStudentFragment : Fragment() {

    val database = FirebaseDatabase.getInstance().reference
    private var _binding: FragmentListStudentBinding? = null
    private val binding get() = _binding!!
    private lateinit var userListRecyclerView: RecyclerView
    private lateinit var userListSearchView: SearchView
    private lateinit var spinner: Spinner
    val userList = mutableListOf<StudentProfile>()
    private var option = 0
    lateinit var progressDialog: ProgressDialog
    var textQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        progressDialog = ProgressDialog.show(requireContext(), "Uploading", "Please wait...", true)
        _binding = FragmentListStudentBinding.inflate(inflater, container, false)
        userListRecyclerView = binding.userListRecyclerView
        userListSearchView = binding.searchView
        spinner = binding.spinner
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val activity = context as AppCompatActivity
        activity.supportActionBar?.title = "Student List"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = context as AppCompatActivity
        activity.supportActionBar?.title = "Student List"
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val layoutManager = LinearLayoutManager(context)
        userListRecyclerView.layoutManager = layoutManager
        getDataAndSetupAdapter()
        userListSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                lifecycleScope.launch {
                    filter(newText)
                }
                textQuery = newText
                return true
            }
        })

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                option = position
                lifecycleScope.launch {
                    progressDialog.show()
                    withContext(Dispatchers.Main) {
                        filter(textQuery)
                    }
                    progressDialog.dismiss()
                }
                // selectedValue değişkeni seçilen değere karşılık gelen indis ile güncellenir
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Hiçbir öğe seçilmediğinde yapılacak işlemler buraya yazılır.
            }
        }


    }

    private fun getDataAndSetupAdapter() {
        val myRef = database.child("profilestudent").ref
        progressDialog.show()
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                if (dataSnapshot.exists()) {

                    for (userSnapshot in dataSnapshot.children) {
                        val user = userSnapshot.getValue(StudentProfile::class.java)
                        user?.let {
                            it.userName = userSnapshot.key ?: ""
                            userList.add(it)
                        }
                    }

                    val adapter = UserListAdapter(userList,this@ListStudentFragment)
                    userListRecyclerView.adapter = adapter
                }
                progressDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Log.d("ListUserFragment", "Verileri alma işlemi başarısız oldu.")
            }
        })
    }

    private fun filter(text: String) {
        val filteredList = mutableListOf<StudentProfile>()
        for (user in userList) {
            if (comperator(user, text)) {
                filteredList.add(user)
            }
        }
        val adapter = UserListAdapter(filteredList, this@ListStudentFragment)
        userListRecyclerView.adapter = adapter
    }
    private fun comperator(studentProfile: StudentProfile, text: String):Boolean{
        var secText = ""
        when(option){
            0 -> {
                secText = studentProfile.name
            }
            1 -> {
                secText = studentProfile.surName
            }
            2 -> {
                secText = studentProfile.workInfo
            }
            else -> {
                secText = studentProfile.situation!!.situation
            }
        }

        return secText.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))
    }
    override fun onPause() {
        super.onPause()
        userList.clear()
    }

}
