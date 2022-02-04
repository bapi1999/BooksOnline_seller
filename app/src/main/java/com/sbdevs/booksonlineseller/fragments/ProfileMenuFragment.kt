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


                R.id.myAccountFragment ->{
                    fragmentAction = ProfileMenuFragmentDirections.actionProfileMenuFragment2ToMyAccountFragment()
                    findNavController().navigate(fragmentAction)
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