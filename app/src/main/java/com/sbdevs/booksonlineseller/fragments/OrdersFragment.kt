package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.FragmentOrdersBinding

class OrdersFragment : Fragment() {
    private var _binding:FragmentOrdersBinding? = null
    private val binding get () = _binding!!

    private val loadingDialog = LoadingDialog()

    private lateinit var bottomSheetDialog:BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)

        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view: View = layoutInflater.inflate(R.layout.ar_order_filter_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        binding.imageButton.setOnClickListener {
            bottomSheetDialog.show()
        }




        return binding.root
    }


}