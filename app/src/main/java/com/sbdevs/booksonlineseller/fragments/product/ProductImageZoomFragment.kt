package com.sbdevs.booksonlineseller.fragments.product

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.adapters.ProductZoomImageAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentProductImageZoomBinding

class ProductImageZoomFragment : Fragment() {
    private var _binding:FragmentProductImageZoomBinding?=null
    private val binding get() = _binding!!
    private lateinit var productImgViewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductImageZoomBinding.inflate(inflater, container, false)

        productImgViewPager = binding.imageZoomPager

        val productImgList = arguments?.getStringArrayList("image_list")

        val adapter = productImgList?.let { ProductZoomImageAdapter(it) }
        productImgViewPager.adapter = adapter
        binding.dotsIndicator.setViewPager2(productImgViewPager)

        return binding.root
    }

}