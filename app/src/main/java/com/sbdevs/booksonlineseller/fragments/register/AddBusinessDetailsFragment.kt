package com.sbdevs.booksonlineseller.fragments.register

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import com.sbdevs.booksonlineseller.activities.MainActivity
import com.sbdevs.booksonlineseller.databinding.FragmentAddBankDetailsBinding
import com.sbdevs.booksonlineseller.databinding.FragmentAddBusinessDetailsBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class AddBusinessDetailsFragment : Fragment() {
    private var _binding: FragmentAddBusinessDetailsBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private val storage = Firebase.storage
    private val storageReference = storage.reference

    private lateinit var businessType: TextInputLayout
    private lateinit var businessNameInput: TextInputLayout
    private lateinit var addressLine1Input: TextInputLayout
    private lateinit var townOrVillInput: TextInputLayout
    private lateinit var pincodeInput: TextInputLayout
    private lateinit var businessState: TextInputLayout
    lateinit var autoCompleteType: AutoCompleteTextView
    lateinit var autoCompleteState: AutoCompleteTextView
    private lateinit var image: ImageView

    private val args: AddBusinessDetailsFragmentArgs by navArgs()



    var fileUri: Uri? = null
    private var cameFrom: String? = null

    private val loadingDialog = LoadingDialog()
    private var newNotificationLong:Long = 0L

    private val startForProfileImageResult =
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
                    Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddBusinessDetailsBinding.inflate(inflater, container, false)
        val businessLay = binding.businessDetails

        loadingDialog.show(childFragmentManager, "Show")

        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO){
                firebaseFirestore.collection("USERS")
                    .document(user!!.uid)
                    .collection("SELLER_DATA")
                    .document("SELLER_DATA").get().addOnSuccessListener {
                        val newNotification = it.get("new_notification").toString().toLong()
                        newNotificationLong = newNotification

                    }.addOnFailureListener {
                        Log.e("Notification","${it.message}")
                    }.await()
            }
            withContext(Dispatchers.Main){
                delay(2000)
                loadingDialog.dismiss()
            }
            withContext(Dispatchers.Main){
                //binding.businessDetails.textView28.text = newNotificationLong.toString()
            }





        }



        businessType = businessLay.businessType
        businessNameInput = businessLay.businessName
        addressLine1Input = businessLay.businessAddressLine1
        townOrVillInput = businessLay.cityOrVill
        pincodeInput = businessLay.pincode
        businessState = businessLay.state
        autoCompleteType = businessLay.autoCompleteType
        autoCompleteState = businessLay.autoCompleteState

        cameFrom = args.cameFrom


        binding.proceedBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager, "Show")
            checkAllDetails()
        }

        binding.skipBtn.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
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
                    loadingDialog.show(childFragmentManager, "Show")
                    startForProfileImageResult.launch(intent)
                }
        }



        return binding.root
    }



    override fun onResume() {
        super.onResume()

        autoCompleteType = binding.businessDetails.autoCompleteType
        val addressTypeList = resources.getStringArray(R.array.business_type)
        val typeAdapter = ArrayAdapter(context!!, R.layout.le_dropdown_item, addressTypeList)
        autoCompleteType.setAdapter(typeAdapter)

        autoCompleteState = binding.businessDetails.autoCompleteState
        val stateList = resources.getStringArray(R.array.india_states)
        val sateAdapter = ArrayAdapter(context!!, R.layout.le_dropdown_item, stateList)
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
        if (!checkType() or !checkName() or !checkAddress() or !checkTownVill()
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



        val sellerDataMap: MutableMap<String, Any> = HashMap()
        sellerDataMap["new_notification"] = (newNotificationLong+1)


        //(not completed, did not added address)


        val sellerRef = firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")

            sellerRef.document("BUSINESS_DETAILS").set(businessDetailsMap)
            .addOnSuccessListener {
                Log.i("UpdateBusinessDetails", "successfully updated")
            }.addOnFailureListener {
                Log.e("UpdateBusinessDetails", "${it.message}")
            }


        sellerRef.document("SELLER_DATA")
            .collection("NOTIFICATION").add(notificationMap)
            .addOnSuccessListener { Log.i("Notification","Successfully added") }
            .addOnFailureListener { Log.e("Notification","${it.message}") }

        sellerRef.document("SELLER_DATA").update(sellerDataMap)
            .addOnSuccessListener { Log.i("New_Notification","Successfully added") }
            .addOnFailureListener { Log.e("New_Notification","${it.message}") }

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
                            if (cameFrom == null) {
                                val mainActivityIntent = Intent(context, MainActivity::class.java)
                                startActivity(mainActivityIntent)
                                activity?.finish()
                            } else {
                                val action = AddBusinessDetailsFragmentDirections.actionAddBusinessDetailsFragment2ToMyAccountFragment()
                                findNavController().navigate(action)
                            }
                        }.addOnFailureListener { e ->
                            Log.e("get Product Thumbnail url", "${e.message}")
                        }
                }.addOnFailureListener {
                    Log.e("uploadThumbnail", "${it.message}")
                }
            }
    }



}