package ca.trinityonhale.donationkiosk.module.trinityonhale

import com.google.gson.JsonArray
import retrofit2.http.GET

interface TrinityOnHaleService {

    @GET("slides.json")
    suspend fun getSlides(): JsonArray
}