package ca.trinityonhale.donationkiosk.module.stripe.provider

import android.util.Log
import ca.trinityonhale.donationkiosk.module.stripeserver.StripeBackendService
import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TokenProvider(
    private val stripeBackendService: StripeBackendService
): ConnectionTokenProvider {

    companion object {
        private const val TAG = "TokenProvider"
    }

    override fun fetchConnectionToken(callback: ConnectionTokenCallback) {

        Log.d(TAG, "Trying to fetch connection token")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connectionToken = stripeBackendService.createConnectionToken()

                Log.d(TAG, "Connection token: ${connectionToken?.secret}")

                if (connectionToken != null) {
                    callback.onSuccess(connectionToken.secret)
                } else {
                    callback.onFailure(ConnectionTokenException("Failed to create connection token"))
                }
            } catch (e: ConnectionTokenException) {
                callback.onFailure(e)
            }
        }
    }
}