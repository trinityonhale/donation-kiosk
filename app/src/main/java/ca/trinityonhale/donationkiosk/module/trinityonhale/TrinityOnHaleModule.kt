package ca.trinityonhale.donationkiosk.module.trinityonhale

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TrinityOnHaleModule {

    private const val BASE_URL = "https://trinityonhale.github.io/kiosk-slides/"

    @Provides
    @Singleton
    fun provideTrinityOnHaleService(): TrinityOnHaleService {

        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(logInterceptor)
                    .build()
            )
            .build()
            .create(TrinityOnHaleService::class.java)
    }
}