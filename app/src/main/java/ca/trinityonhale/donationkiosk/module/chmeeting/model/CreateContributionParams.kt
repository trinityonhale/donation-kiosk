package ca.trinityonhale.donationkiosk.module.chmeeting.model

import com.google.gson.annotations.SerializedName

data class CreateContributionParams (
    @SerializedName("person"            ) var person          : Person?          = Person(),
    @SerializedName("note"              ) var note            : String?          = null,
    @SerializedName("payment_method_id" ) var paymentMethodId : Int?             = null,
    @SerializedName("payment_method"    ) var paymentMethod   : String?          = null,
    @SerializedName("funds"             ) var funds           : ArrayList<Funds> = arrayListOf(),
    @SerializedName("payment_provider"  ) var paymentProvider : String?          = null
)