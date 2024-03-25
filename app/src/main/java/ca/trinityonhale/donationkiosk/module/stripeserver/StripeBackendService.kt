package ca.trinityonhale.donationkiosk.module.stripeserver

import android.content.SharedPreferences
import android.util.Log
import ca.trinityonhale.donationkiosk.PREFS_STRIPE_API_KEY
import com.stripe.StripeClient
import com.stripe.model.Customer
import com.stripe.model.PaymentIntent
import com.stripe.model.terminal.ConnectionToken
import com.stripe.param.CustomerCreateParams
import com.stripe.param.CustomerListParams
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.PaymentIntentUpdateParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StripeBackendService {

    private val TAG = "StripeBackendService"
    private var client: StripeClient? = null

    @Inject
    constructor(
        sharedPreferences: SharedPreferences
    ) {
        val apiKey = sharedPreferences.getString(PREFS_STRIPE_API_KEY, null)

        if (apiKey != null) {
            client = StripeClient(apiKey)
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == PREFS_STRIPE_API_KEY) {
                val apiKey = sharedPreferences.getString(PREFS_STRIPE_API_KEY, null)
                if (apiKey != null) {
                    setApiKey(apiKey)
                }
            }
        }
    }

    private fun checkHasClient () {
        if (client == null) {
            throw Exception("Stripe API key not set")
        }
    }

    /**
     * Set the API key for the Stripe client
     *
     * @param apiKey The API key to use
     * @return void
     */
    private fun setApiKey(apiKey: String) {
        client = StripeClient(apiKey)
    }

    suspend fun createConnectionToken(): ConnectionToken? {
        checkHasClient()

        return makeRequest {
            var connectionToken = client!!.terminal().connectionTokens().create()
            Log.d(TAG, "Connection token created: $connectionToken")
            return@makeRequest connectionToken
        }.getOrNull()
    }

    private suspend fun <T> makeRequest(block: () -> T): Result<T> {
        return withContext(Dispatchers.IO) {
            runCatching {
                block()
            }
        }
    }

    suspend fun createPaymentIntent(amount: Long): Result<PaymentIntent> {
        checkHasClient()

        Log.d(TAG, "Creating payment intent for $amount")

        var params = PaymentIntentCreateParams.Builder()
            .setAmount(amount)
            .setCurrency("cad")
            .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.AUTOMATIC)
            .addAllPaymentMethodType(listOf(
                "card_present",
                "interac_present",
            ))

        return makeRequest {
            client!!.paymentIntents().create(params.build())
        }
    }

//    fun capturePaymentIntent(paymentIntentId: String): PaymentIntent? {
//        return client.paymentIntents().capture(paymentIntentId)
//    }

    /**
     * Create a customer with the given email address
     *
     * @param params The parameters for the customer
     * @return The ID of the created customer
     */
    suspend fun createCustomer(params: CustomerCreateParams): Result<String?> {
        checkHasClient()

        Log.d(TAG, "Creating customer for ${params.email}")

        var customer: Customer? = makeRequest {
            client!!.customers().list(
                CustomerListParams.builder().setEmail(params.email).build()
            )
        }.fold(
            onSuccess = {
                if (it.data.isNotEmpty()) it.data[0] else null
            },
            onFailure = {
                return Result.failure(it)
            }
        )

        if (customer != null) {
            return Result.success(customer.id)
        }

        return makeRequest {
            client!!.customers().create(params).id
        }
    }

    suspend fun updatePaymentIntent(paymentIntentId: String, params: PaymentIntentUpdateParams): Result<PaymentIntent> {
        checkHasClient()

        return makeRequest {
            client!!.paymentIntents().update(paymentIntentId, params)
        }
    }

    fun cancelPaymentIntent(paymentIntentId: String): PaymentIntent? {
        checkHasClient()

        return client!!.paymentIntents().cancel(paymentIntentId)
    }
}