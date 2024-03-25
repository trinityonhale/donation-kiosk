package ca.trinityonhale.donationkiosk.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import ca.trinityonhale.donationkiosk.MainActivity
import ca.trinityonhale.donationkiosk.databinding.FragmentDonateBinding
import ca.trinityonhale.donationkiosk.databinding.ViewDonateBinding
import ca.trinityonhale.donationkiosk.databinding.ViewProcessingBinding
import ca.trinityonhale.donationkiosk.databinding.ViewPresentCardBinding
import ca.trinityonhale.donationkiosk.databinding.ViewPaymentFailedBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.regex.Matcher
import java.util.regex.Pattern

@AndroidEntryPoint
class DonateFragment : Fragment() {

    companion object {
        const val TAG: String = "DonateFragment"
    }

    private lateinit var viewModel: DonateViewModel
    private lateinit var binding: FragmentDonateBinding
    private lateinit var viewDonate: ViewDonateBinding
    private lateinit var viewProcessing: ViewProcessingBinding
    private lateinit var viewPresentCard: ViewPresentCardBinding
    private lateinit var viewPaymentFailed: ViewPaymentFailedBinding

    class CurrencyFormatInputFilter : InputFilter {
        private var mPattern: Pattern = Pattern.compile("(0|[1-9]+[0-9]*)?(\\.[0-9]{0,2})?")

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int,
        ): CharSequence? {
            val result = (dest.subSequence(0, dstart)
                .toString() + source.toString()
                    + dest.subSequence(dend, dest.length))
            val matcher: Matcher = mPattern.matcher(result)
            return if (!matcher.matches()) dest.subSequence(dstart, dend) else null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentDonateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(DonateViewModel::class.java)

        viewDonate = binding.viewDonate
        viewProcessing = binding.viewProcessing
        viewPresentCard = binding.viewPresentCard
        viewPaymentFailed = binding.viewPaymentFailed

        viewModel.paymentStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                DonateViewModel.PaymentStatus.CREATING -> {
                    viewDonate.root.visibility = View.GONE
                    viewProcessing.root.visibility = View.VISIBLE
                }
                DonateViewModel.PaymentStatus.CREATED -> {
                    lifecycleScope.launch {
                        viewModel.collectAndConfirmPayment()
                    }
                }
                DonateViewModel.PaymentStatus.COLLECTING -> {
                    viewProcessing.root.visibility = View.GONE
                    viewPresentCard.root.visibility = View.VISIBLE
                }
                DonateViewModel.PaymentStatus.CONFIRMING -> {
                    viewPresentCard.root.visibility = View.GONE
                    viewProcessing.root.visibility = View.VISIBLE
                }
                DonateViewModel.PaymentStatus.FAILED -> {
                    viewProcessing.root.visibility = View.GONE
                    viewPaymentFailed.root.visibility = View.VISIBLE
                }
                DonateViewModel.PaymentStatus.SUCCESS -> {
                    var activity = requireActivity() as MainActivity
                    activity.navigateTo(DonateSuccessFragment.TAG, DonateSuccessFragment(viewModel.paymentIntent!!))
                }
                DonateViewModel.PaymentStatus.READY -> {}
            }
        }

        addListenersForViewDonate()
        addListenersForViewPaymentFailed()
    }

    private fun addListenersForViewPaymentFailed() {
        viewPaymentFailed.btnRetry.setOnClickListener {
            viewModel.paymentStatus.postValue(DonateViewModel.PaymentStatus.CREATED)
        }

        viewPaymentFailed.btnCancel.setOnClickListener {

            lifecycleScope.launch {
                viewModel.cancelPaymentIntent()
                // we just return to the previous screen
                requireActivity().onBackPressed()
            }
        }
    }

    private fun addListenersForViewDonate() {

        // handle back button
        viewDonate.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // handle donate button
        viewDonate.btnDonate.setOnClickListener {
            viewDonate.editTextAmount.clearFocus()
            lifecycleScope.launch {
                try {
                    viewModel.createAndRetrievePaymentIntent(
                        BigDecimal(viewDonate.editTextAmount.text.toString())
                    )
                } catch (e: Exception) {
                    Toast.makeText(requireActivity(), "An error occurred when creating the order", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewDonate.editTextAmount.filters = arrayOf(CurrencyFormatInputFilter())
        viewDonate.editTextAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewDonate.btnDonate.isEnabled = s.toString().isNotEmpty()
            }
        })
    }
}