package ca.trinityonhale.donationkiosk.module.chmeeting.model

import com.google.gson.annotations.SerializedName

data class CreateContributionResponse (

    @SerializedName("id"                ) var id              : Int?             = null,
    @SerializedName("amount"            ) var amount          : Double?          = null,
    @SerializedName("fee"               ) var fee             : Int?             = null,
    @SerializedName("payment_method_id" ) var paymentMethodId : Int?             = null,
    @SerializedName("payment_method"    ) var paymentMethod   : String?          = null,
    @SerializedName("created_on"        ) var createdOn       : String?          = null,
    @SerializedName("batch_num"         ) var batchNum        : String?          = null,
    @SerializedName("check_num"         ) var checkNum        : String?          = null,
    @SerializedName("paid_on"           ) var paidOn          : String?          = null,
    @SerializedName("note"              ) var note            : String?          = null,
    @SerializedName("person_id"         ) var personId        : String?          = null,
    @SerializedName("first_name"        ) var firstName       : String?          = null,
    @SerializedName("last_name"         ) var lastName        : String?          = null,
    @SerializedName("envelope_number"   ) var envelopeNumber  : String?          = null,
    @SerializedName("funds"             ) var funds           : ArrayList<Funds> = arrayListOf(),
    @SerializedName("payment_provider"  ) var paymentProvider : String?          = null

)