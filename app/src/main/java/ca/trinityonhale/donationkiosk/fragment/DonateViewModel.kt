package ca.trinityonhale.donationkiosk.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.trinityonhale.donationkiosk.module.stripeserver.StripeBackendService
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.SimulatedCard
import com.stripe.stripeterminal.external.models.SimulatedCardType
import com.stripe.stripeterminal.external.models.SimulatorConfiguration
import com.stripe.stripeterminal.external.models.TerminalException
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class DonateViewModel @Inject constructor(
    val stripeBackendService: StripeBackendService
) : ViewModel() {

    companion object {
        private const val TAG = "DonateViewModel"
    }

    var paymentIntent: PaymentIntent? = null
    var paymentStatus: MutableLiveData<PaymentStatus> = MutableLiveData(PaymentStatus.READY)

    enum class PaymentStatus {
        READY,
        CREATING,
        CREATED,
        COLLECTING,
        CONFIRMING,
        FAILED,
        SUCCESS,
    }

    /**
     * Create and retrieve a PaymentIntent from the backend
     *
     * @param amount The amount to charge
     * @return The PaymentIntent
     */
    suspend fun createAndRetrievePaymentIntent(amount: BigDecimal) {
        val amountInCents = amount.multiply(BigDecimal(100)).toLong()

        // enable simulated card failures
        // setSimulatedCardFailures()

        paymentStatus.postValue(PaymentStatus.CREATING)
        try {
            val clientSecret = createPaymentIntentClientSecret(amountInCents)
            paymentIntent = retrievePaymentIntent(clientSecret)
            paymentStatus.postValue(PaymentStatus.CREATED)
        } catch (e: Exception) {
            paymentStatus.postValue(PaymentStatus.READY)
        }
    }

    private fun setSimulatedCardFailures() {
        Terminal.getInstance().simulatorConfiguration = SimulatorConfiguration(
            simulatedCard = SimulatedCard("4000000000000002")
        )
    }

    private suspend fun createPaymentIntentClientSecret(amountInCents: Long): String {
        Log.d(TAG, "Creating payment intent for $amountInCents cents")

        return stripeBackendService.createPaymentIntent(amountInCents).fold(
            onSuccess = { paymentIntent ->
                Log.d(TAG, "Payment intent created: ${paymentIntent.clientSecret}")
                paymentIntent.clientSecret
            },
            onFailure = { error ->
                // TODO: Handle error
                Log.d(TAG, "Failed to create payment intent: ${error.message}")
                throw error
            })
    }

    private suspend fun retrievePaymentIntent(clientSecret: String?): PaymentIntent {
        return suspendCoroutine { continuation ->
            Terminal.getInstance().retrievePaymentIntent(clientSecret!!, object: PaymentIntentCallback {
                override fun onFailure(e: TerminalException) {
                    Log.d(TAG, "Failed to retrieve payment intent: ${e.message}")
                    continuation.resumeWith(Result.failure(e))
                }

                override fun onSuccess(paymentIntent: PaymentIntent) {
                    Log.d(TAG, "Payment intent retrieved: ${paymentIntent.id}")
                    continuation.resumeWith(Result.success(paymentIntent))
                }
            })
        }
    }

    private suspend fun collectPaymentMethod(paymentIntent: PaymentIntent): PaymentIntent {

        Log.d(TAG, "Collecting payment method for ${paymentIntent.id}")

        return suspendCoroutine { continuation ->
            Terminal.getInstance().collectPaymentMethod(paymentIntent,
                object : PaymentIntentCallback {
                    override fun onSuccess(paymentIntent: PaymentIntent) {
                        // Placeholder for confirming paymentIntent
                        Log.d(TAG, "Payment method collected: ${paymentIntent.id}")
                        continuation.resumeWith(Result.success(paymentIntent))
                    }

                    override fun onFailure(exception: TerminalException) {
                        // Placeholder for handling exception
                        Log.d(TAG, "Failed to collect payment method: ${exception.message}")
                        continuation.resumeWith(Result.failure(exception))
                    }
                })
        }
    }

    private suspend fun confirmPayment(paymentIntent: PaymentIntent): PaymentIntent {

        Log.d(TAG, "Confirming payment intent: ${paymentIntent.id}")

        return suspendCoroutine { continuation ->
            Terminal.getInstance().confirmPaymentIntent(paymentIntent, object : PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    Log.d(TAG, "Payment intent confirmed: ${paymentIntent.id}")

                    continuation.resumeWith(Result.success(paymentIntent))
                }

                override fun onFailure(exception: TerminalException) {
                    // Placeholder for handling the exception
                    Log.d(TAG, "Failed to confirm payment intent: ${exception.message}")

                    continuation.resumeWith(Result.failure(exception))
                }
            })
        }
    }

    suspend fun collectAndConfirmPayment() {
        if (paymentIntent == null) {
            throw Exception("PaymentIntent is null")
        }

        try {
            paymentStatus.postValue(PaymentStatus.COLLECTING)
            paymentIntent = collectPaymentMethod(paymentIntent!!)
            paymentStatus.postValue(PaymentStatus.CONFIRMING)
            paymentIntent = confirmPayment(paymentIntent!!)
            paymentStatus.postValue(PaymentStatus.SUCCESS)
        } catch (e: TerminalException) {
            paymentStatus.postValue(PaymentStatus.FAILED)
        }
    }

    suspend fun cancelPaymentIntent(): PaymentIntent {

        return suspendCoroutine { continuation ->
            Terminal.getInstance().cancelPaymentIntent(paymentIntent!!, object: PaymentIntentCallback {
                override fun onFailure(e: TerminalException) {
                    Log.d(TAG, "Failed to cancel payment intent: ${e.message}")
                    continuation.resumeWith(Result.failure(e))
                }

                override fun onSuccess(paymentIntent: PaymentIntent) {
                    Log.d(TAG, "Payment intent cancelled: ${paymentIntent.id}")
                    continuation.resumeWith(Result.success(paymentIntent))
                }
            })
        }
    }
}