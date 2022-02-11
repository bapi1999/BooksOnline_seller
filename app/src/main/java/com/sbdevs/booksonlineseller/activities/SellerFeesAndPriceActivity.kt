package com.sbdevs.booksonlineseller.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout
import com.sbdevs.booksonlineseller.databinding.ActivitySellerFeesAndPriceBinding

class SellerFeesAndPriceActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySellerFeesAndPriceBinding
    private lateinit var enterPriceInput:TextInputLayout
    private lateinit var calculateBtn:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerFeesAndPriceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enterPriceInput = binding.lay1.enterPriceInputLayout
        calculateBtn = binding.lay1.calculateBtn

        calculateBtn.setOnClickListener {
            checkInput()
        }


    }

    private fun checkInput(){
        val value = enterPriceInput.editText!!.text.toString().trim()
        if (value.isEmpty()){
            enterPriceInput.error = "Field can't be empty"

        }else{
            enterPriceInput.error = ""
           calculateProfit(value.toInt())
        }
    }

    private fun calculateProfit(sellingPrice:Int){
        val lay2 = binding.lay2
        val platformCharge = sellingPrice/10F
        val pickupCharge = 30F
        val profit:Float = sellingPrice - platformCharge-pickupCharge

        lay2.sellingPrice.text = sellingPrice.toString()
        lay2.commissionFee.text = platformCharge.toString()
        lay2.deliveryFee.text = pickupCharge.toString()
        lay2.totalProfit.text = profit.toString()

    }


}