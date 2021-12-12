package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.FragmentMyAccountBinding

class MyAccountFragment : Fragment() {
    private var _binding:FragmentMyAccountBinding? =null
    private val binding get() = _binding!!
    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var businessNameString:String
    private lateinit var businessTypeString:String

    private lateinit var businessNameText:TextView
    private lateinit var businessTypeText:TextView
    private lateinit var accountNumber:TextView
    private lateinit var idscCode:TextView
    private lateinit var verifyIcon:ImageView
    private lateinit var verifyText:TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAccountBinding.inflate(inflater, container, false)

        isUserVerified()
        getBankDetails()
        businessNameText = binding.layBusiness.businessName
        businessTypeText = binding.layBusiness.businessType
        verifyText = binding.lay1.verifyText
        verifyIcon = binding.lay1.verifyIcon


        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.noBusinessBtn.setOnClickListener {
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToAddBusinessDetailsFragment2("my_account")
            findNavController().navigate(action)
        }
        binding.layBank.noAccountAddNew.setOnClickListener {
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToAddBankDetailsFragment()
            findNavController().navigate(action)
        }
    }

    private fun isUserVerified(){
        val docRef = firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("BUSINESS_DETAILS").get().addOnSuccessListener {
                val isBusinessAdded = it.getBoolean("Is_BusinessDetail_Added")!!

                if (isBusinessAdded){
                    binding.warningText.visibility = View.GONE
                    binding.businessContainer.visibility = View.VISIBLE
                    binding.noBusinessBtn.visibility = View.GONE
                    val businessName = it.get("Business_name")!!.toString()
                    val businessType = it.get("Business_type")!!.toString()
                    val isVerified = it.getBoolean("is_address_verified")!!
                    if (isVerified){
                        verifyText.text = "verified"
                        binding.lay1.verifyContainer.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.successGreen)
                        verifyIcon.setImageResource(R.drawable.ic_check_circle_outline_24)
                    }else{
                        verifyText.text = "Not verified"
                        verifyIcon.visibility = View.GONE
                        binding.lay1.verifyContainer.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.brikeRed)

                    }
                    businessNameString = businessName
                    businessTypeString = businessType

                    businessTypeText.text = businessType
                    businessNameText.text = businessName

                }else{
                    binding.warningText.visibility = View.VISIBLE
                    binding.businessContainer.visibility = View.GONE
                    binding.noBusinessBtn.visibility = View.VISIBLE
                }


            }
    }

    private fun getBankDetails(){
        val docRef = firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("BANK_DETAILS").get()
            .addOnSuccessListener {
                val isBankAdded = it.getBoolean("Is_BankDetail_Added")!!
                if (isBankAdded){
                    binding.layBank.detailsContainer.visibility = View.VISIBLE
                    binding.layBank.noAccountAddNew.visibility = View.GONE
                    binding.warningBankText.visibility = View.GONE
                    val bankAccount = it.get("Bank_account_number")!!.toString()
                    val ifscCode = it.get("Bank_ifsc_code")!!.toString()
                    val googlePayUpi = it.get("GooglePay_upi")!!.toString()
                    val phonePeUpi = it.get("PhonePe_upi")!!.toString()


                }else{
                    binding.warningBankText.visibility = View.VISIBLE
                    binding.layBank.detailsContainer.visibility = View.GONE
                    binding.layBank.noAccountAddNew.visibility = View.VISIBLE
                }
            }
    }



}