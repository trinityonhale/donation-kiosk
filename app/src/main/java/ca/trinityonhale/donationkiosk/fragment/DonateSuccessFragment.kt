package ca.trinityonhale.donationkiosk.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import ca.trinityonhale.donationkiosk.databinding.FragmentDonateSuccessBinding
import com.stripe.stripeterminal.external.models.PaymentIntent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DonateSuccessFragment(
    private val paymentIntent: PaymentIntent
) : Fragment() {

    companion object {
        const val TAG: String = "DonateSuccessFragment"
    }

    private lateinit var viewModel: DonateSuccessViewModel
    private lateinit var binding: FragmentDonateSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentDonateSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(DonateSuccessViewModel::class.java)
        viewModel.paymentIntent.postValue(paymentIntent)

        // simply go back
        binding.btnAnonymousDonate.setOnClickListener {
            requireActivity().onBackPressed()
        }

        var bindingForm = binding.viewForm
        var bindingSubmitSuccess = binding.viewSubmitSuccess

        bindingSubmitSuccess.root.visibility = View.GONE

        bindingForm.btnSubmit.setOnClickListener {
            Log.d("DonateSuccessFragment", "Submit button clicked")

            lifecycleScope.launch {
                Log.d("DonateSuccessFragment", "Submitting form")
                try {
                    viewModel.submitForm().runCatching {
                        bindingForm.root.visibility = View.INVISIBLE
                        binding.btnAnonymousDonate.visibility = View.GONE
                        bindingSubmitSuccess.root.visibility = View.VISIBLE

                        lifecycleScope.launch {
                            kotlinx.coroutines.delay(5000)
                            requireActivity().onBackPressed()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to submit form", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bindingForm.txtFirstName.addTextChangedListener {
            viewModel.setFirstName(it.toString())
        }

        bindingForm.txtLastName.addTextChangedListener {
            viewModel.setLastName(it.toString())
        }

        bindingForm.txtEmail.addTextChangedListener {
            viewModel.setEmail(it.toString())
        }

        bindingForm.txtNote.addTextChangedListener {
            viewModel.setNote(it.toString())
        }
    }
}
