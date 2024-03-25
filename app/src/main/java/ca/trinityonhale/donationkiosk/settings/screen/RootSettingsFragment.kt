package ca.trinityonhale.donationkiosk.settings.screen

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import ca.trinityonhale.donationkiosk.MainViewModel
import ca.trinityonhale.donationkiosk.PREFS_SELECTED_READER_SERIAL_NUMBER
import ca.trinityonhale.donationkiosk.R
import ca.trinityonhale.donationkiosk.module.encryptedprefs.SharedPreferenceDataStore
import ca.trinityonhale.donationkiosk.module.stripe.model.DiscoveryMethod
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.models.ConnectionStatus
import com.stripe.stripeterminal.external.models.Reader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RootSettingsFragment: PreferenceFragmentCompat() {

    private lateinit var terminalDiscoveryCategory: PreferenceCategory
    private lateinit var defaultTerminalPref: Preference

    // Register the permissions callback to handles the response to the system permissions dialog.
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        ::onPermissionResult,
    )

    private fun onPermissionResult(permissions: Map<String, Boolean>) {
        // If none of the requested permissions were declined, start the discovery process.
        if (permissions.none { !it.value }) {
            startDiscovery()
        } else {
            // (requireActivity() as MainActivity).onCancelDiscovery()
        }
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission(discoveryMethod: DiscoveryMethod): Boolean {
        val hasGpsModule = requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
        val locationPermission = if (hasGpsModule) {
            Manifest.permission.ACCESS_FINE_LOCATION
        } else {
            Manifest.permission.ACCESS_COARSE_LOCATION
        }

        val ungrantedPermissions = buildList {
            if (!isGranted(locationPermission)) add(locationPermission)

            if (discoveryMethod == DiscoveryMethod.BLUETOOTH_SCAN && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!isGranted(Manifest.permission.BLUETOOTH_SCAN)) add(Manifest.permission.BLUETOOTH_SCAN)
                if (!isGranted(Manifest.permission.BLUETOOTH_CONNECT)) add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }.toTypedArray()

        return if (ungrantedPermissions.isNotEmpty()) {
            // If we don't have all the required permissions yet, request them before doing anything else.
            requestPermissionLauncher.launch(ungrantedPermissions)
            false
        } else {
            true
        }
    }

    private fun startDiscovery() {
        if (checkPermission(viewModel.getDiscoveryMethod())) {
             viewModel.startDiscovery()
        }
    }

    companion object {
        const val TAG = "RootSettingsFragment"
    }

    @Inject lateinit var encryptedSharedPreferences: SharedPreferences

    private val viewModel: MainViewModel by activityViewModels()

    private fun hasDefaultTerminal(): Boolean {
        return encryptedSharedPreferences.contains(PREFS_SELECTED_READER_SERIAL_NUMBER)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultTerminalPref = findPreference<Preference>("default_terminal")!!
        terminalDiscoveryCategory = findPreference<PreferenceCategory>("terminal_discovery_category")!!

        if (!hasDefaultTerminal()) {
            defaultTerminalPref?.title = "No terminal selected"
            defaultTerminalPref?.summary = "Please select a terminal below"
        } else {
            val terminal = Terminal.getInstance().connectedReader

            val batteryLevel = terminal?.batteryLevel?.times(100)?.toInt() ?: 0

            defaultTerminalPref?.title = terminal?.label ?: terminal?.deviceType?.deviceName ?: "Unknown"
            defaultTerminalPref?.summary =
                "${terminal?.serialNumber ?: "Unknown"} / Battery: ${batteryLevel}%"
        }

        // only start discovery if we are not already connected
        if (Terminal.getInstance().connectionStatus !== ConnectionStatus.CONNECTED) {
            startDiscovery()
        }

        viewModel.discoveredReaders.observe(viewLifecycleOwner) {
            refreshDiscoveredReaders(it)
        }
    }

    private fun refreshDiscoveredReaders(readers: List<Reader>) {
        val preferences = readers.map { reader ->

            Preference(requireContext()).apply {
                title = reader.label ?: reader.deviceType.deviceName
                summary = reader.serialNumber

                setOnPreferenceClickListener {
                    viewModel.connectToReader(reader)
                    true
                }
            }
        }

        terminalDiscoveryCategory.removeAll()
        preferences.forEach { terminalDiscoveryCategory.addPreference(it) }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = SharedPreferenceDataStore(encryptedSharedPreferences)
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}