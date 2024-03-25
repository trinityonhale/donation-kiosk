package ca.trinityonhale.donationkiosk.module.chmeeting.model

import com.google.gson.annotations.SerializedName

data class Funds (

    @SerializedName("fund_name" ) var fundName : String? = null,
    @SerializedName("amount"    ) var amount   : Double?    = null,
    @SerializedName("fund_id"   ) var fundId   : Int?    = null

)
