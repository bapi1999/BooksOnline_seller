package com.sbdevs.booksonlineseller.fragments.product

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.FragmentProductImageZoomBinding

class ProductImageZoomFragment : Fragment() {
    private var _binding:FragmentProductImageZoomBinding?=null
    private val binding get() = _binding!!
    private val args:ProductImageZoomFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductImageZoomBinding.inflate(inflater, container, false)
        val url = args.imageUrl
        val imageView = binding.touchImage

        Glide.with(requireContext()).load(url).placeholder(R.drawable.as_square_placeholder).into(imageView)

        return binding.root
    }

}