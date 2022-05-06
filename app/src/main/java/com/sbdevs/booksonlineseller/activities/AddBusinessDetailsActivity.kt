package com.sbdevs.booksonlineseller.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.ActivityAddBusinessDetailsBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddBusinessDetailsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAddBusinessDetailsBinding

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private val storage = Firebase.storage
    private val storageReference = storage.reference

    private lateinit var businessType: TextInputLayout
    private lateinit var businessNameInput: TextInputLayout
    private lateinit var businessPhoneInput: TextInputLayout
    private lateinit var addressLine1Input: TextInputLayout
    private lateinit var townOrVillInput: TextInputLayout
    private lateinit var pincodeInput: TextInputLayout
    private lateinit var businessState: TextInputLayout
    lateinit var autoCompleteType: AutoCompleteTextView
    lateinit var autoCompleteState: AutoCompleteTextView
    private lateinit var image: ImageView

    var fileUri: Uri? = null
    private var cameFrom: String? = null

    private val loadingDialog = LoadingDialog()
    private var newNotificationLong:Long = 0L

    private val startForAddressProf =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    fileUri = data?.data!!
                    loadingDialog.dismiss()
                    Glide.with(this).load(fileUri)
                        .placeholder(R.drawable.as_square_placeholder).into(image)
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBusinessDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val businessLay = binding.businessDetails
        businessType = businessLay.businessType
        businessNameInput = businessLay.businessName
        businessPhoneInput = businessLay.businessPhoneNo
        addressLine1Input = businessLay.businessAddressLine1
        townOrVillInput = businessLay.cityOrVill
        pincodeInput = businessLay.pincode
        businessState = businessLay.state
        autoCompleteType = businessLay.autoCompleteType
        autoCompleteState = businessLay.autoCompleteState

        cameFrom = null// args.cameFrom

        if (cameFrom == null) {
            binding.lay1.skipBtn.visibility = View.VISIBLE
        } else {
            binding.lay1.skipBtn.visibility = View.GONE
        }



        image = businessLay.uploadAddressProfBtn

        businessLay.uploadAddressProfBtn.setOnClickListener {
            ImagePicker.with(this)
                .compress(1024)
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    loadingDialog.show(supportFragmentManager, "Show")
                    startForAddressProf.launch(intent)
                }
        }


    }

    override fun onStart() {
        super.onStart()

        binding.proceedBtn.setOnClickListener {
            loadingDialog.show(supportFragmentManager, "Show")
            checkAllDetails()
        }

        binding.lay1.skipBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        autoCompleteType = binding.businessDetails.autoCompleteType
        val addressTypeList = resources.getStringArray(R.array.business_type)
        val typeAdapter = ArrayAdapter(this, R.layout.item_dropdown, addressTypeList)
        autoCompleteType.setAdapter(typeAdapter)

        autoCompleteState = binding.businessDetails.autoCompleteState
        val stateList = resources.getStringArray(R.array.india_states)
        val sateAdapter = ArrayAdapter(this, R.layout.item_dropdown, stateList)
        autoCompleteState.setAdapter(sateAdapter)
    }



    private fun checkName(): Boolean {
        val businessNameString: String = businessNameInput.editText?.text.toString()
        return if (businessNameString.isEmpty()) {
            businessNameInput.isErrorEnabled = true
            businessNameInput.error = "Field can't be empty"
            false
        } else {
            businessNameInput.error = null
            true

        }
    }

    private fun checkPhone(): Boolean {
        val businessPhoneString: String = businessPhoneInput.editText?.text.toString()
        return if (businessPhoneString.isEmpty()) {
            businessPhoneInput.isErrorEnabled = true
            businessPhoneInput.error = "Field can't be empty"
            false
        } else {

            if(businessPhoneString.length==10){
                businessPhoneInput.isErrorEnabled = false
                businessPhoneInput.error = null
                true
            }else{
                businessPhoneInput.isErrorEnabled = true
                businessPhoneInput.error = "Must be 10 digit number"
                false
            }

        }
    }

    private fun checkType(): Boolean {
        val typeString: String = autoCompleteType.text.toString()
        return if (typeString.isEmpty()) {
            businessType.isErrorEnabled = true
            businessType.error = "Select Type"
            false
        } else {
            businessType.error = null
            true
        }
    }

    private fun checkAddress(): Boolean {
        val addressString: String = addressLine1Input.editText?.text.toString()
        return if (addressString.isEmpty()) {
            addressLine1Input.isErrorEnabled = true
            addressLine1Input.error = "Field can't be empty"
            false
        } else {
            addressLine1Input.error = null
            true

        }
    }

    private fun checkTownVill(): Boolean {
        val townVillString: String = townOrVillInput.editText?.text.toString()
        return if (townVillString.isEmpty()) {
            townOrVillInput.isErrorEnabled = true
            townOrVillInput.error = "Field can't be empty"
            false
        } else {
            townOrVillInput.error = null
            true

        }
    }

    private fun checkPincode(): Boolean {
        val pincodeString: String = pincodeInput.editText?.text.toString()
        return if (pincodeString.isEmpty()) {
            pincodeInput.isErrorEnabled = true
            pincodeInput.error = "Field can't be empty"
            false
        } else {
            if (pincodeString.length == 6) {
                pincodeInput.error = null
                true
            } else {
                pincodeInput.isErrorEnabled = true
                pincodeInput.error = "Must be 6 digits"
                false
            }


        }
    }

    private fun checkState(): Boolean {
        val stateString: String = autoCompleteState.text.toString()
        return if (stateString.isEmpty()) {
            businessState.isErrorEnabled = true
            businessState.error = "Select State"
            false
        } else {
            businessState.error = null
            true

        }
    }

    private fun checkImageUri(): Boolean {
        return if (fileUri == null) {
            binding.businessDetails.errorMessageText.text = "Select image"
            binding.businessDetails.errorMessageText.visibility = View.VISIBLE
            false
        } else {
            binding.businessDetails.errorMessageText.text = ""
            binding.businessDetails.errorMessageText.visibility = View.GONE
            true

        }
    }

    private fun checkAllDetails() {
        if (!checkType() or !checkName() or !checkPhone() or !checkAddress() or !checkTownVill()
            or !checkPincode() or !checkState() or !checkImageUri()
        ) {

            loadingDialog.dismiss()

            return
        } else {

            lifecycleScope.launch(Dispatchers.IO) {

                withContext(Dispatchers.IO) {
                    updateBusinessDetails()

                    uploadAddressProf()

                }

            }

        }
    }

    private fun updateBusinessDetails() {

        val addressMap: MutableMap<String, Any> = HashMap()
        addressMap["Address_line_1"] = addressLine1Input.editText?.text.toString()
        addressMap["Town_Vill"] = townOrVillInput.editText?.text.toString()
        addressMap["PinCode"] = pincodeInput.editText?.text.toString()
        addressMap["State"] = autoCompleteState.text.toString()

        val businessDetailsMap: MutableMap<String, Any> = HashMap()
        businessDetailsMap["Business_name"] = businessNameInput.editText?.text.toString()
        businessDetailsMap["Business_phone"] = businessPhoneInput.editText?.text.toString()

        businessDetailsMap["is_address_verified"] = false
        businessDetailsMap["Business_type"] = autoCompleteType.text.toString()
        businessDetailsMap["Is_BusinessDetail_Added"] = true
        businessDetailsMap["address"] = addressMap


        val verificationNoti: String = getString(R.string.address_verification_notification)

        val notificationMap: MutableMap<String, Any> = HashMap()
        notificationMap["date"] = FieldValue.serverTimestamp()
        notificationMap["description"] = verificationNoti
        notificationMap["image"] = ""
        notificationMap["order_id"] = ""
        notificationMap["seen"] = false

        //(not completed, did not added address)


        val sellerRef = firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")

        sellerRef.document("BUSINESS_DETAILS").set(businessDetailsMap)
            .addOnSuccessListener {
                Log.i("UpdateBusinessDetails", "successfully updated")
            }.addOnFailureListener {
                Log.e("UpdateBusinessDetails", "${it.message}")
            }


        firebaseFirestore.collection("USERS").document(user!!.uid)
            .collection("SELLER_NOTIFICATIONS").add(notificationMap)
            .addOnSuccessListener { Log.i("Notification","Successfully added") }
            .addOnFailureListener { Log.e("Notification","${it.message}") }


    }

    private fun uploadAddressProf() {
        val mRef: StorageReference =
            storageReference.child("image/" + user!!.uid + "/" + "address/").child("addressP_prof")
        mRef.putFile(fileUri!!)
            .addOnCompleteListener {
                mRef.downloadUrl.addOnSuccessListener {
                    val uploadThumbMap: MutableMap<String, Any> = java.util.HashMap()
                    uploadThumbMap["Address_prof_image"] = it.toString()


                    firebaseFirestore.collection("USERS")
                        .document(user!!.uid).collection("SELLER_DATA")
                        .document("BUSINESS_DETAILS")
                        .update(uploadThumbMap)
                        .addOnSuccessListener {
                            Log.i("Update Product Thumbnail", "Successfully updated")
                            loadingDialog.dismiss()
                            val mainActivityIntent = Intent(this, MainActivity::class.java)
                            startActivity(mainActivityIntent)
                            finish()
                        }.addOnFailureListener { e ->
                            Log.e("get Product Thumbnail url", "${e.message}")
                        }
                    sendVerificationRequest(it.toString())
                }.addOnFailureListener {
                    Log.e("uploadThumbnail", "${it.message}")
                }
            }
    }

    private fun sendVerificationRequest(addressProf:String){

        val addressMap: MutableMap<String, Any> = HashMap()
        addressMap["Address_line_1"] = addressLine1Input.editText?.text.toString()
        addressMap["Town_Vill"] = townOrVillInput.editText?.text.toString()
        addressMap["PinCode"] = pincodeInput.editText?.text.toString()
        addressMap["State"] = autoCompleteState.text.toString()

        val verifyMap: MutableMap<String, Any> = HashMap()
        verifyMap["SellerId"] = FieldValue.serverTimestamp()
        verifyMap["time"] = FieldValue.serverTimestamp()
        verifyMap["address_prof"] = addressProf
        verifyMap["address_map"] = addressMap
        verifyMap["Business_name"] = businessNameInput.editText?.text.toString()
        verifyMap["is_verified"] = false
        verifyMap["request_type"] = "new" //new , verified, canceled

        firebaseFirestore.collection("VRIFICATION_REQUEST").add(verifyMap)
    }


}