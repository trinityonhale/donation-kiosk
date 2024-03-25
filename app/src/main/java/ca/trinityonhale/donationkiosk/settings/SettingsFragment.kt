package ca.trinityonhale.donationkiosk.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ca.trinityonhale.donationkiosk.MainActivity
import ca.trinityonhale.donationkiosk.R
import ca.trinityonhale.donationkiosk.settings.screen.RootSettingsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    companion object {
        const val TAG: String = "SettingsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    private fun setSupportActionbar() {
        var toolbar = view?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        var activity = requireActivity() as MainActivity

        activity.setSupportActionBar(toolbar)
        // add back arrow to toolbar
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true);
        activity.supportActionBar?.setDisplayShowHomeEnabled(true);
    }

    private fun removeSupportActionbar() {
        var activity = requireActivity() as MainActivity
        activity.setSupportActionBar(null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSupportActionbar()

        // get childfragment manager
        childFragmentManager.beginTransaction()
            .replace(R.id.settings_container, RootSettingsFragment())
            .commitNow()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeSupportActionbar()
    }
}