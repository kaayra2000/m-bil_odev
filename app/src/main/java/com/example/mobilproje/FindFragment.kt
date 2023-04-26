package com.example.mobilproje

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mobilproje.databinding.FragmentCreateProfileBinding
import com.example.mobilproje.databinding.FragmentFindBinding


class FindFragment : Fragment() {
    private var _binding: FragmentFindBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindBinding.inflate(inflater, container, false)

        binding.findStudentButton.setOnClickListener {
            findNavController().navigate(R.id.action_findFragment_to_listStudentFragment)
        }

        binding.announcementButton.setOnClickListener {

        }

        binding.photoButton.setOnClickListener {

        }

        return binding.root

    }
}