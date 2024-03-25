package ca.trinityonhale.donationkiosk.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.trinityonhale.donationkiosk.R

class KioskDisabledFragment : Fragment() {

    companion object {
        fun newInstance() = KioskDisabledFragment()
    }

    private lateinit var viewModel: KioskDisabledViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_kiosk_disabled, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(KioskDisabledViewModel::class.java)
        // TODO: Use the ViewModel
    }

}