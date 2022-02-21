package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.FragmentMyAccountBinding
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.tasks.await

class MyAccountFragment : Fragment() {
    private var _binding:FragmentMyAccountBinding? =null
    private val binding get() = _binding!!
    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var businessNameText:TextView
    private lateinit var businessTypeText:TextView
    private lateinit var verifyIcon:ImageView
    private lateinit var verifyText:TextView
    private lateinit var profileImage:CircleImageView
    private val gone = View.GONE
    private val visible = View.VISIBLE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAccountBinding.inflate(inflater, container, false)

        isUserVerified()
        getBankDetails()
        getUerDetails()

        businessNameText = binding.layBusiness.businessName
        businessTypeText = binding.layBusiness.businessType
        verifyText = binding.lay1.verifyText
        verifyIcon = binding.lay1.verifyIcon
        profileImage = binding.lay1.userImage


        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.noBusinessBtn.setOnClickListener {
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToAddBusinessDetailsFragment2("my_account")
            findNavController().navigate(action)
        }
        binding.addNewUpi.setOnClickListener {
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToAddBankDetailsFragment()
            findNavController().navigate(action)
        }

        binding.editUpi.setOnClickListener {
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToAddBankDetailsFragment()
            findNavController().navigate(action)
        }
    }

    private fun isUserVerified(){
        val docRef = firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("BUSINESS_DETAILS").get().addOnSuccessListener {
                val isBusinessAdded = it.getBoolean("Is_BusinessDetail_Added")!!
                val isVerified = it.getBoolean("is_address_verified")!!

                if (isBusinessAdded){
                    binding.warningText.visibility = View.GONE
                    binding.businessContainer.visibility = View.VISIBLE
                    binding.noBusinessBtn.visibility = View.GONE
                    val businessName = it.get("Business_name")!!.toString()
                    val businessType = it.get("Business_type")!!.toString()
                    val businessPhone= it.get("Business_phone")!!.toString()

                    val businessAddress:MutableMap<String,Any> = (it.get("address") as MutableMap<String, Any>?)!!
                    val addlissLine1 = businessAddress["Address_line_1"].toString()
                    val town = businessAddress["Town_Vill"].toString()
                    val pincode= businessAddress["PinCode"].toString()
                    val state = businessAddress["State"].toString()


                    if (isVerified){
                        verifyText.text = "verified"
                        binding.lay1.verifyContainer.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.successGreen)
                        verifyIcon.setImageResource(R.drawable.ic_check_circle_outline_24)
                    }else{
                        verifyText.text = "Not verified"
                        verifyIcon.visibility = View.GONE
                        binding.lay1.verifyContainer.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)

                    }
//                    businessNameString = businessName
//                    businessTypeString = businessType

                    businessNameText.text = businessName
                    businessTypeText.text = businessType

                    binding.layBusiness.buyerPhone.text = businessPhone
                    binding.layBusiness.buyerAddress.text = addlissLine1
                    binding.layBusiness.buyerTownAndPin.text = "$town, $pincode"
                    binding.layBusiness.buyerState.text = state
                    binding.layBusiness.buyerPhone.text


                }else{
                    binding.warningText.visibility = View.VISIBLE
                    binding.businessContainer.visibility = View.GONE
                    binding.noBusinessBtn.visibility = View.VISIBLE

                    verifyText.text = "Not verified"
                    verifyIcon.visibility = View.GONE
                    binding.lay1.verifyContainer.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)

                }


            }
    }

    private fun getBankDetails(){
        firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("BANK_DETAILS").get()
            .addOnSuccessListener {
                val isBankAdded = it.getBoolean("Is_BankDetail_Added")!!
                if (isBankAdded){
                    binding.upiContainer.visibility = visible
                    binding.editUpi.visibility = visible
                    binding.addNewUpi.visibility = gone
                    binding.warningBankText.visibility =gone

                    val upiId = it.getString("UPI_id").toString()
                    binding.uipIdText.text = upiId


                }else{
                    binding.upiContainer.visibility = gone
                    binding.editUpi.visibility = gone
                    binding.addNewUpi.visibility = visible
                    binding.warningBankText.visibility = visible
                }
            }
    }

    private fun getUerDetails(){
        firebaseFirestore.collection("USERS").document(user!!.uid)
            .get().addOnSuccessListener {

                val name = it.get("name")!!.toString().trim()
                val email = it.get("email")!!.toString()
                val phone = it.get("mobile_No")!!.toString()
                val profile= it.get("mobile_No")!!.toString().trim()

                if (name==""){
                    binding.lay1.userName.text = "No Name"
                }else{
                    binding.lay1.userName.text = name
                }

                if (profile.isNullOrEmpty()){
                   Glide.with(requireParentFragment()).load(profile).placeholder(R.drawable.as_circle_placeholder).into(profileImage)
                }

                binding.lay1.userMail.text = email
                binding.lay1.userPhone.text = phone


            }.addOnFailureListener {
                Log.e("get Notification time","${it.message}")
            }
    }



}