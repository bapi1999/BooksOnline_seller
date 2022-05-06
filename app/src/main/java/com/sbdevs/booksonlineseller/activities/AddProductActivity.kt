package com.sbdevs.booksonlineseller.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.sbdevs.booksonlineseller.databinding.ActivityAddProductBinding


import com.sbdevs.booksonlineseller.R
import java.util.*

import androidx.navigation.findNavController
import com.sbdevs.booksonlineseller.otherclass.OrderSharedData


class AddProductActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAddProductBinding

    private val gone = View.GONE
    private val visible = View.VISIBLE
    private val inVisible = View.INVISIBLE

    private lateinit var addBusinessDetailsBtn: Button
    private lateinit var warningMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addBusinessDetailsBtn= binding.noBusinessLay.button2
        warningMessage=binding.noBusinessLay.textView3
        addBusinessDetailsBtn.setOnClickListener {

        }

        val st1 = "You are not eligible to add products."

        if (OrderSharedData.isSellerVerified){

            if (OrderSharedData.isAddressVerified){
                binding.noBusinessContainer.visibility = gone
                binding.fragmentLayout.visibility = visible
                findNavController(R.id.add_product_host_fragment)

            }else{
                binding.noBusinessContainer.visibility = visible
                binding.fragmentLayout.visibility = inVisible
                addBusinessDetailsBtn.visibility = gone
                val st = getString(R.string.seller_address_not_verified)
                warningMessage.text ="$st1 $st"
            }

        }else{
            binding.noBusinessContainer.visibility = visible
            binding.fragmentLayout.visibility = inVisible
            val st = getString(R.string.you_are_not_a_verified_seller)

            warningMessage.text = "$st1 $st"
            addBusinessDetailsBtn.visibility = visible
            OrderSharedData.isSellerVerified = false

        }





    }





}