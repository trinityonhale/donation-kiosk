package ca.trinityonhale.donationkiosk.module.chmeeting

import ca.trinityonhale.donationkiosk.module.chmeeting.model.CampaignsResponse
import ca.trinityonhale.donationkiosk.module.chmeeting.model.CreateContributionParams
import ca.trinityonhale.donationkiosk.module.chmeeting.model.CreateContributionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ChmeetingService {

    @GET("campaigns")
    suspend fun getCampaigns(): CampaignsResponse

    @POST("contributions")
    suspend fun postContribution(
        @Body createContributionParams: CreateContributionParams
    ) : CreateContributionResponse
}