package com.sbdevs.booksonlineseller.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.activities.AddProductActivity
import com.sbdevs.booksonlineseller.activities.MyStoreActivity
import com.sbdevs.booksonlineseller.activities.PoliciesActivity
import com.sbdevs.booksonlineseller.activities.SellerFeesAndPriceActivity
import com.sbdevs.booksonlineseller.databinding.FragmentProfileMenuBinding


class ProfileMenuFragment : Fragment() {

    private var _binding:FragmentProfileMenuBinding? =null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileMenuBinding.inflate(inflater, container, false)

        binding.navView.setNavigationItemSelectedListener {
            val fragmentAction: NavDirections

            when(it.itemId){


                R.id.allProductsFragment ->{
                    fragmentAction = ProfileMenuFragmentDirections.actionProfileMenuFragment2ToMyProductFragment()
                    findNavController().navigate(fragmentAction)
                    true
                }

                R.id.addProducts->{
                    val intent = Intent(context, AddProductActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.earningFragment->{
                    fragmentAction = ProfileMenuFragmentDirections.actionProfileMenuFragment2ToMyEarningFragment()
                    findNavController().navigate(fragmentAction)
                    true
                }

//                R.id.myStore ->{
//                    val intent = Intent(context, MyStoreActivity::class.java)
//                    startActivity(intent)
//                    true
//                }

                R.id.myAccountFragment ->{
                    fragmentAction = ProfileMenuFragmentDirections.actionProfileMenuFragment2ToMyAccountFragment()
                    findNavController().navigate(fragmentAction)
                    true
                }

                R.id.profit_calculator ->{
                    val intent = Intent(context, SellerFeesAndPriceActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.termCondition ->{
                    val myIntent = Intent(requireContext(), PoliciesActivity::class.java)
                    myIntent.putExtra("PolicyCode",1)// 1 = Terms and services
                    startActivity(myIntent)
                    true
                }

                R.id.privacy_policy ->{
                    val myIntent = Intent(requireContext(), PoliciesActivity::class.java)
                    myIntent.putExtra("PolicyCode",2)// 2 = Privacy Policy
                    startActivity(myIntent)
                    true
                }

                R.id.return_policy ->{
                    val myIntent = Intent(requireContext(), PoliciesActivity::class.java)
                    myIntent.putExtra("PolicyCode",3)//3 = Return Policy
                    startActivity(myIntent)
                    true
                }



                else->{
                    false
                }


            }

        }

        return binding.root
    }

}