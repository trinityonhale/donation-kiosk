package ca.trinityonhale.donationkiosk.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.trinityonhale.donationkiosk.module.chmeeting.ChmeetingService
import ca.trinityonhale.donationkiosk.module.chmeeting.model.CreateContributionParams
import ca.trinityonhale.donationkiosk.module.chmeeting.model.Funds
import ca.trinityonhale.donationkiosk.module.chmeeting.model.Person
import ca.trinityonhale.donationkiosk.module.stripeserver.StripeBackendService
import com.stripe.param.CustomerCreateParams
import com.stripe.param.PaymentIntentUpdateParams
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.models.PaymentIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class DonateSuccessViewModel @Inject constructor(
    private val stripeBackendService: StripeBackendService,
    private val chmeetingService: ChmeetingService
) : ViewModel() {

    companion object {
        private const val TAG = "DonateSuccessViewModel"
    }

    private val _firstName = MutableStateFlow("")
    private val _lastName = MutableStateFlow("")
    private val _email = MutableStateFlow("")
    private val _note = MutableStateFlow("")
    val paymentIntent: MutableLiveData<PaymentIntent> = MutableLiveData()

    fun setFirstName(firstName: String) {
        _firstName.value = firstName
    }

    fun setLastName(lastName: String) {
        _lastName.value = lastName
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setNote(note: String) {
        _note.value = note
    }

    suspend fun submitForm() {

        Log.d(TAG, "Submitting form")

        Log.d(TAG, """
            First name: ${_firstName.value}
            Last name: ${_lastName.value}
            Email: ${_email.value}
            Note: ${_note.value}
        """.trimIndent())

        var params = CustomerCreateParams
                        .builder()
                        .setName("${_firstName.value} ${_lastName.value}")

        if (_email.value.isNotEmpty()) {
            params.setEmail(_email.value)
        }

        try {
            val customerId = stripeBackendService.createCustomer(params.build()).getOrNull()

            Log.d(TAG, "Customer created: $customerId")
            updatePaymentIntent(customerId, _note.value)
            Log.d(TAG, "Payment intent updated")

            if (_firstName.value.isNotEmpty() && _lastName.value.isNotEmpty() && _email.value.isNotEmpty()) {
                Log.d(TAG, "Adding contribution to chmeetings")
                addContributionToChmeetings()
            }
        } catch (e: Exception) {
            Log.e("DonateSuccessViewModel", "Failed to submit form", e)
            Log.e("DonateSuccessViewModel", "Error message: ${e.message}")
        }
    }

    private suspend fun updatePaymentIntent(customerId: String?, note: String) {
        val paymentIntentId = paymentIntent.value?.id!!

        var builder = PaymentIntentUpdateParams.builder()

        if (customerId != null) {
            builder.setCustomer(customerId)
        }

        if (note.isNotEmpty()) {
            builder.setMetadata(mapOf("note" to note))
        }

        stripeBackendService.updatePaymentIntent(
            paymentIntentId,
            builder.build()
        )
    }

    private suspend fun addContributionToChmeetings() {
        val amount = paymentIntent.value?.amount!!

        val string = chmeetingService.postContribution(CreateContributionParams(
            person = Person(
                firstName = _firstName.value,
                lastName = _lastName.value,
                email = _email.value
            ),
            note = _note.value,
            paymentMethod = "Stripe_Kiosk",
            funds = arrayListOf(
                Funds(
                    fundName = "Kiosk Donation",
                    amount = amount.toDouble() / 100.0,
                )
            ),
            paymentProvider = "Stripe_Kiosk"
        ))

        Log.d(TAG, "Contribution added to chmeetings: $string")
    }
}