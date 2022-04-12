package com.sbdevs.booksonlineseller.fragments

import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.activities.AddBusinessDetailsActivity
import com.sbdevs.booksonlineseller.activities.EditBusinessDetailsActivity
import com.sbdevs.booksonlineseller.databinding.FragmentMyAccountBinding
import de.hdodenhof.circleimageview.CircleImageView
import java.io.Serializable

class MyAccountFragment : Fragment() {
    private var _binding:FragmentMyAccountBinding? =null
    private val binding get() = _binding!!
    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth
    private val user = firebaseAuth.currentUser

    private lateinit var businessNameText:TextView
    private lateinit var businessTypeText:TextView
    private lateinit var verifyIcon:ImageView
    private lateinit var verifyText:TextView
    private lateinit var profileImage:CircleImageView
    private val gone = View.GONE
    private val visible = View.VISIBLE
    private var businessAddressMap:MutableMap<String,Any> = HashMap()
    private var businessContentList:ArrayList<String> = ArrayList()
    private var isAddressAvailable = false
    private var addressProfImage = ""

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.noBusinessBtn.setOnClickListener {
            val businessIntent = Intent (requireContext(),AddBusinessDetailsActivity::class.java)
            startActivity(businessIntent)
        }
        binding.addNewUpi.setOnClickListener {
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToAddBankDetailsFragment()
            findNavController().navigate(action)
        }

        binding.editUpi.setOnClickListener {
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToAddBankDetailsFragment()
            findNavController().navigate(action)
        }

        binding.layBusiness.editAddressBtn.setOnClickListener {
            if (isAddressAvailable){
                val editIntent = Intent(context, EditBusinessDetailsActivity::class.java)
                editIntent.putStringArrayListExtra("businessContentList",businessContentList)
                editIntent.putExtra("address",businessAddressMap as Serializable)
                editIntent.putExtra("addressImage",addressProfImage)
                startActivity(editIntent)
            }else{
                val newIntent = Intent(context, AddBusinessDetailsActivity::class.java)
                startActivity(newIntent)
            }

        }

        binding.logoutBtn.setOnClickListener {
            logOutUser()
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

                    val businessType = it.get("Business_type")!!.toString()
                    val businessName = it.get("Business_name")!!.toString()
                    val businessPhone= it.get("Business_phone")!!.toString()
                    addressProfImage= it.getString("Address_prof_image")!!.toString()

                    businessContentList.add(0,businessType)
                    businessContentList.add(1,businessName)
                    businessContentList.add(2,businessPhone)

                    businessAddressMap = (it.get("address") as MutableMap<String, Any>?)!!

                    isAddressAvailable = businessAddressMap.isNotEmpty()

                    val addlissLine1 = businessAddressMap["Address_line_1"].toString()
                    val town = businessAddressMap["Town_Vill"].toString()
                    val pincode= businessAddressMap["PinCode"].toString()
                    val state = businessAddressMap["State"].toString()


                    if (isVerified){
                        verifyText.text = "verified"
                        binding.lay1.verifyContainer.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.successGreen)
                        verifyIcon.setImageResource(R.drawable.ic_check_circle_outline_24)
                    }else{
                        verifyText.text = "Not verified"
                        verifyIcon.visibility = View.GONE
                        binding.lay1.verifyContainer.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)

                    }

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

    private fun logOutUser(){
        val userId = FirebaseAuth.getInstance().currentUser
        if (userId != null){
            firebaseAuth.signOut()
            deleteToken(userId.uid)
        }
    }
    private fun deleteToken(uid:String){

        FirebaseDatabase.getInstance()
            .getReference("Seller_Tokens")
            .child(uid)
            .removeValue()

    }


}