package com.example.mobilproje

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentCreateProfileBinding
import com.example.mobilproje.databinding.FragmentFindBinding


class FindFragment : Fragment() {
    private var _binding: FragmentFindBinding? = null
    private val binding get() = _binding!!
    lateinit var email: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = context as AppCompatActivity
        activity.supportActionBar?.title = "Find"
    }
    override fun onResume() {
        super.onResume()
        val activity = context as AppCompatActivity
        activity.supportActionBar?.title = "Find"
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        email = arguments?.getString("email").toString()
        _binding = FragmentFindBinding.inflate(inflater, container, false)

        binding.findStudentButton.setOnClickListener {
            findNavController().navigate(R.id.action_findFragment_to_listStudentFragment)
        }

        binding.announcementButton.setOnClickListener {
            findNavController().navigate(R.id.action_findFragment_to_announcementListFragment)
        }

        binding.photoButton.setOnClickListener {
            val parts = email.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val username = parts[0]
            val bundle = bundleOf("userName" to username)
            findNavController().navigate(R.id.action_findFragment_to_galleryFragment,bundle)

        }

        return binding.root

    }
}