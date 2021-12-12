package com.sbdevs.booksonlineseller.activities

import android.app.Activity
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.sbdevs.booksonlineseller.databinding.ActivityAddProductBinding


import androidx.appcompat.content.res.AppCompatResources
import java.lang.StringBuilder
import com.google.android.material.textfield.TextInputLayout
import com.sbdevs.booksonlineseller.R
import java.util.*

import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlin.collections.ArrayList
import android.widget.RadioButton
import androidx.core.view.isEmpty
import androidx.navigation.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class AddProductActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAddProductBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.add_product_host_fragment)



    }





}