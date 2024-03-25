package ca.trinityonhale.donationkiosk

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import ca.trinityonhale.donationkiosk.fragment.CoverFragment
import ca.trinityonhale.donationkiosk.fragment.InitializingFragment
import ca.trinityonhale.donationkiosk.fragment.NoNetworkFragment
import ca.trinityonhale.donationkiosk.listener.TerminalEventListener
import ca.trinityonhale.donationkiosk.module.stripe.provider.TokenProvider
import ca.trinityonhale.donationkiosk.settings.SettingsFragment
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.models.ConnectionStatus
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import com.stripe.stripeterminal.external.models.TerminalException
import com.stripe.stripeterminal.log.LogLevel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : FullScreenAppCompatActivity() {

    @Inject lateinit var encryptedSharedPreferences: SharedPreferences
    @Inject lateinit var tokenProvider: TokenProvider

    private val viewModel: MainViewModel by viewModels()

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var terminalEventListener: TerminalEventListener

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigateTo(InitializingFragment.TAG, InitializingFragment())

        acquireBluetoothPermission()

        lifecycleScope.launch {
            initializeTerminal()
        }

//        viewModel.checkHasNetworkConnection()

        if (!isAllConfigured()) {
            navigateTo(SettingsFragment.TAG, SettingsFragment(),
                addToBackStack = true
            )
        } else {
            connectDefaultReader()
        }

        viewModel.connectionStatus.observe(this) { status ->
            when (status) {
                ConnectionStatus.CONNECTED -> {
                    navigateTo(CoverFragment.TAG, CoverFragment())
                }
                ConnectionStatus.NOT_CONNECTED -> {
                    Log.d(TAG, "Reader not connected")
                }
                ConnectionStatus.CONNECTING -> {
                    Log.d(TAG, "Reader connecting")
                }
            }
        }
    }

    private fun acquireBluetoothPermission() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            BluetoothAdapter.getDefaultAdapter()?.let { adapter ->
                if (!adapter.isEnabled) {
                    adapter.enable()
                }
            }
        } else {
            Log.w(TAG, "Failed to acquire Bluetooth permission")
        }
    }

    fun initializeTerminal() {
        Log.d(TAG, "Initializing terminal")
        terminalEventListener = TerminalEventListener(this)

        // FIXME: this might not throw the exception
        try {
            if (!Terminal.isInitialized()) {
                Terminal.initTerminal(
                    applicationContext,
                    LogLevel.VERBOSE,
                    tokenProvider,
                    terminalEventListener
                )
            }
        } catch (e: ConnectionTokenException) {
            Log.e(TAG, "Failed to create connection token", e)
        } catch (e: TerminalException) {
            Log.e(TAG, "Error retrieving connection token", e)
        }
    }

    private fun isStripeApiKeyConfigured(): Boolean {
        return encryptedSharedPreferences.contains(PREFS_STRIPE_API_KEY)
    }

    private fun isAllConfigured(): Boolean {
        return encryptedSharedPreferences.contains(PREFS_STRIPE_API_KEY) &&
        encryptedSharedPreferences.contains(PREFS_USE_READER_SIMULATOR) &&
        encryptedSharedPreferences.contains(PREFS_READER_DISCOVERY_METHOD) &&
        encryptedSharedPreferences.contains(PREFS_SELECTED_READER_SERIAL_NUMBER)
    }

    /**
     * Navigate to the given fragment.
     *
     * @param fragment Fragment to navigate to.
     */
    fun navigateTo(
        tag: String,
        fragment: Fragment,
        replace: Boolean = true,
        addToBackStack: Boolean = false
    ) {
        val frag = supportFragmentManager.findFragmentByTag(tag) ?: fragment

        supportFragmentManager
            .beginTransaction()
            .apply {
                if (replace) {
                    replace(R.id.container, frag, tag)
                } else {
                    add(R.id.container, frag, tag)
                }

                if (addToBackStack) {
                    addToBackStack(tag)
                }
            }
            .commitAllowingStateLoss()
    }

    fun popTopFragment() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
    }

    @Deprecated("Use navigateTo(tag: String, fragment: Fragment) instead")
    fun navigateTo(fragment: Fragment, addToBackStack: Boolean = false) {
        if (addToBackStack) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "Back pressed")
        Log.d(TAG, "Tags: ${supportFragmentManager.fragments.map { it.tag }}")

        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }

    @RequiresPermission(
        anyOf = [
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        ],
    )
    fun connectDefaultReader() {
        if (isAllConfigured()) {
            viewModel.startDiscovery()
        }
    }
}