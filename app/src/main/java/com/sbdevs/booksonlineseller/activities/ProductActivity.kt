package com.sbdevs.booksonlineseller.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.ActivityProductBinding
import com.sbdevs.booksonlineseller.fragments.order.OrdersFragment
import com.sbdevs.booksonlineseller.fragments.product.ProductDetailsFragment

class ProductActivity : AppCompatActivity() {
    private lateinit var binding:ActivityProductBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productFragment = ProductDetailsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.product_host_fragment,productFragment).commit()


    }
}