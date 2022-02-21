package com.sbdevs.booksonlineseller.fragments.register

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.FragmentAddBankDetailsBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class AddBankDetailsFragment : Fragment() {

    private var _binding :FragmentAddBankDetailsBinding? =null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private val storage = Firebase.storage
    private val storageReference = storage.reference
    private lateinit var errorMessage:TextView


    private lateinit var upiIdInput:TextInputLayout
    private lateinit var upiScreenShot:ImageView


    var fileUri: Uri? = null
    private val loadingDialog = LoadingDialog()


    private val startForUpiScreenShoot =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    fileUri = data?.data!!

                    Glide.with(this).load(fileUri)
                        .placeholder(R.drawable.as_square_placeholder).into(upiScreenShot)
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
        _binding = FragmentAddBankDetailsBinding.inflate(inflater, container, false)

        val bankDetails = binding.bankDetails

        errorMessage = binding.errorMessageText
        upiIdInput = bankDetails.upiIdInput
        upiScreenShot = bankDetails.upiScreenshot


        binding.proceedBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager,"Show")
            checkAllDetails()
        }


        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.bankDetails.upiScreenshot.setOnClickListener{

            ImagePicker.with(this)
                .compress(1024)
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForUpiScreenShoot.launch(intent)
                }
        }

    }



    private fun checkUpi(): Boolean {
        val upiId: String = upiIdInput.editText?.text.toString()
        return if (upiId.isEmpty()) {
            upiIdInput.isErrorEnabled = true
            upiIdInput.error = "Field can't be empty"
            false
        } else {
            upiIdInput.error = null
            true
        }
    }


    private fun checkUPIScreenShot(): Boolean {
        return if (fileUri == null) {

            binding.bankDetails.hintAndError.text ="Select image"
            binding.bankDetails.hintAndError.setTextColor(AppCompatResources.getColorStateList(requireContext(), R.color.red_a700))
            false
        } else {
            binding.bankDetails.hintAndError.text =""
            true

        }
    }


    private fun checkAllDetails() {
        if (!checkUPIScreenShot() or !checkUpi()  ) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_LONG).show()
            loadingDialog.dismiss()
            return
        } else {
            lifecycleScope.launch{

                updateBankDetails()
                uploadQrCode()

            }

        }
    }

    private suspend fun updateBankDetails(){

        val bankDetailsMap: MutableMap<String, Any> = HashMap()
//        bankDetailsMap["Bank_account_number"] = accountNumberInput.editText?.text.toString().trim()
//        bankDetailsMap["Bank_ifsc_code"] = ifscCodeInput.editText?.text.toString().trim()
//        bankDetailsMap["Bank_account_holder"] = ifscCodeInput.editText?.text.toString().trim()

        bankDetailsMap["UPI_id"] = upiIdInput.editText?.text.toString()
        bankDetailsMap["Is_BankDetail_Added"] = true

        firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("BANK_DETAILS").set(bankDetailsMap)
            .addOnSuccessListener {
                Log.i("UpdateBankDetails","successfully updated")
            }.addOnFailureListener {
                Log.e("UpdateBankDetails","${it.message}")
            }.await()
    }

    private suspend fun uploadQrCode() {
        val mRef: StorageReference =
            storageReference.child("image/" + user!!.uid + "/"+"upi/").child("upi_qr_code")
        mRef.putFile(fileUri!!)
            .addOnSuccessListener{
                mRef.downloadUrl.addOnSuccessListener {
                    val uploadThumbMap: MutableMap<String, Any> = java.util.HashMap()
                    uploadThumbMap["UPI_qrCode"] = it.toString()
                    firebaseFirestore.collection("USERS")
                        .document(user!!.uid).collection("SELLER_DATA")
                        .document("BANK_DETAILS")
                        .update(uploadThumbMap)
                        .addOnSuccessListener {
                            Log.i("get Product QR code url", "Successful")
                            errorMessage.visibility = View.VISIBLE
                            errorMessage.text = "UPI added successfully"
                            errorMessage.setTextColor(AppCompatResources.getColorStateList(requireContext(),R.color.indigo_700))
                        }.addOnFailureListener { e ->
                            Log.e("get Product QR code url", "${e.message}")
                            errorMessage.visibility = View.VISIBLE
                            errorMessage.text = "Failed to add UPI"
                            errorMessage.setTextColor(AppCompatResources.getColorStateList(requireContext(),R.color.red_700))
                        }
                }.addOnFailureListener {
                    Log.e("uploadQrcode", "${it.message}")
                }
            }.await()
    }




}