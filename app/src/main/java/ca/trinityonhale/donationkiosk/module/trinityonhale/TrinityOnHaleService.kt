package ca.trinityonhale.donationkiosk.module.trinityonhale

import retrofit2.http.GET

interface TrinityOnHaleService {

    @GET("slides.json")
    suspend fun getSlides(): List<String>
}