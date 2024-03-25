package ca.trinityonhale.donationkiosk.module.chmeeting.model

import com.google.gson.annotations.SerializedName

data class Person (

    @SerializedName("person_id"  ) var personId  : Int?    = null,
    @SerializedName("first_name" ) var firstName : String? = null,
    @SerializedName("last_name"  ) var lastName  : String? = null,
    @SerializedName("email"      ) var email     : String? = null,
    @SerializedName("mobile"     ) var mobile    : String? = null

)
