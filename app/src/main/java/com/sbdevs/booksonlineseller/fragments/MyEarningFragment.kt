package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.FragmentMyEarningBinding


class MyEarningFragment : Fragment() {
    private var _binding:FragmentMyEarningBinding? =null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyEarningBinding.inflate(inflater, container, false)

        binding.lay1.withdrawalBtn.setOnClickListener {
            val action = MyEarningFragmentDirections.actionMyEarningFragmentToWithdrawalFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

}