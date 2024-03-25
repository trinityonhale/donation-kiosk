package ca.trinityonhale.donationkiosk

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.trinityonhale.donationkiosk.module.stripe.model.DiscoveryMethod
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.DiscoveryListener
import com.stripe.stripeterminal.external.callable.ReaderCallback
import com.stripe.stripeterminal.external.callable.ReaderListener
import com.stripe.stripeterminal.external.callable.ReaderReconnectionListener
import com.stripe.stripeterminal.external.models.ConnectionConfiguration
import com.stripe.stripeterminal.external.models.ConnectionStatus
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.TerminalException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: DonationKioskApplication,
    private val encryptedSharedPreferences: SharedPreferences
): ViewModel() {

    companion object {
        private const val TAG = "RootSettingsViewModel"
    }

    private fun saveReaderSerialNumber(reader: Reader) {
        encryptedSharedPreferences.edit().putString(PREFS_SELECTED_READER_SERIAL_NUMBER, reader.serialNumber).apply()
    }

    private fun clearReaderSerialNumber() {
        encryptedSharedPreferences.edit().remove(PREFS_SELECTED_READER_SERIAL_NUMBER).apply()
    }

    private val readerCallback = object: ReaderCallback {
        override fun onFailure(e: TerminalException) {
            // TODO: how to capture the exception
            try {
                clearReaderSerialNumber()
                connectionStatus.postValue(ConnectionStatus.NOT_CONNECTED)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect reader: ${e.message}")
            }
        }

        override fun onSuccess(reader: Reader) {
            saveReaderSerialNumber(reader)
            connectionStatus.postValue(ConnectionStatus.CONNECTED)
        }
    }

    private val readerListener = object: ReaderListener {
    }

    private val readerReconnectionListener = object: ReaderReconnectionListener {
        override fun onReaderReconnectFailed(reader: Reader) {
            clearReaderSerialNumber()
        }

        override fun onReaderReconnectSucceeded(reader: Reader) {
            TODO("Not yet implemented")
        }

    }
    var discoveryTask: Cancelable? = null
    var isDiscovering: MutableLiveData<Boolean> = MutableLiveData(false)
    var discoveredReaders: MutableLiveData<List<Reader>> = MutableLiveData(listOf())
    var connectionStatus: MutableLiveData<ConnectionStatus> = MutableLiveData(ConnectionStatus.CONNECTING)
    var hasNetworkConnection: MutableLiveData<Boolean> = MutableLiveData(false)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // network is available for use
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            hasNetworkConnection.postValue(true)
        }

        // Network capabilities have changed for the network
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)

            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                hasNetworkConnection.postValue(true)
            } else {
                hasNetworkConnection.postValue(false)
            }
        }

        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)
            hasNetworkConnection.postValue(false)
        }
    }

    fun checkHasNetworkConnection() {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()

        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    fun getDiscoveryMethod(): DiscoveryMethod {
        val method = encryptedSharedPreferences.getString(PREFS_READER_DISCOVERY_METHOD, "bluetooth")

        when (method) {
            "bluetooth" -> return DiscoveryMethod.BLUETOOTH_SCAN
            "usb" -> return DiscoveryMethod.USB
            else -> return DiscoveryMethod.BLUETOOTH_SCAN
        }
    }

    fun getIsUseSimulated(): Boolean {
        return encryptedSharedPreferences.getBoolean(PREFS_USE_READER_SIMULATOR, false)
    }

    fun getDefaultReaderSerialNumber(): String? {
        return encryptedSharedPreferences.getString(PREFS_SELECTED_READER_SERIAL_NUMBER, null)
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ],
    )
    fun startDiscovery() {
        isDiscovering.postValue(true)

        if (discoveryTask == null && Terminal.getInstance().connectedReader == null) {

            Log.d(TAG, "Starting discovery task")

            discoveryTask = Terminal
                .getInstance()
                .discoverReaders(
                    config = when (getDiscoveryMethod()) {
                        DiscoveryMethod.BLUETOOTH_SCAN -> DiscoveryConfiguration.BluetoothDiscoveryConfiguration(0, getIsUseSimulated())
//                        DiscoveryMethod.INTERNET -> DiscoveryConfiguration.InternetDiscoveryConfiguration(
//                            location = selectedLocation.value?.id,
//                            isSimulated = isSimulated,
//                        )
                        DiscoveryMethod.USB -> DiscoveryConfiguration.UsbDiscoveryConfiguration(0, getIsUseSimulated())
                    },
                    discoveryListener = object : DiscoveryListener {
                        override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
                            val serialNumber = getDefaultReaderSerialNumber()

                            if (serialNumber != null) {
                                val defaultReader = readers.find { it.serialNumber == serialNumber }
                                if (defaultReader != null) {
                                    connectToReader(defaultReader)
                                }
                            }
                            discoveredReaders.postValue(readers)
                        }
                    },
                    callback = object : Callback {
                        // NOTE: this is only called when a terminal was connected
                        override fun onSuccess() {
                            Log.d(TAG, "Discovery task completed")
                            discoveryTask = null
                        }

                        override fun onFailure(e: TerminalException) {
                            Log.d(TAG, "Discovery task failed: ${e.message}")
                            discoveryTask = null
                        }
                    }
                )
        }
    }

    fun connectToReader(reader: Reader) {

        val locationId = reader.location?.id ?: ""

        when (getDiscoveryMethod()) {
            DiscoveryMethod.BLUETOOTH_SCAN -> {
                Terminal.getInstance().connectBluetoothReader(
                    reader,
                    ConnectionConfiguration.BluetoothConnectionConfiguration(
                        locationId,
                        true,
                        readerReconnectionListener
                    ),
                    readerListener,
                    readerCallback,
                )
            }
            DiscoveryMethod.USB -> {
                Terminal.getInstance().connectUsbReader(
                    reader,
                    ConnectionConfiguration.UsbConnectionConfiguration(
                        locationId,
                        true,
                        readerReconnectionListener
                    ),
                    readerListener,
                    readerCallback,
                )
            }
        }
    }
}