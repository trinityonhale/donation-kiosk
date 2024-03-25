package ca.trinityonhale.donationkiosk.module.chmeeting

import android.content.SharedPreferences
import android.util.Log
import ca.trinityonhale.donationkiosk.PREFS_CHMEETING_API_KEY
import ca.trinityonhale.donationkiosk.module.encryptedprefs.EncryptedPrefsModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module(
    includes = [
        EncryptedPrefsModule::class
    ]
)
@InstallIn(SingletonComponent::class)
object ChmeetingModule {

    private const val BASE_URL = "https://api.chmeetings.com/api/v1/"

    @Provides
    @Singleton
    fun provideChmeetingService(encryptedPrefs: SharedPreferences): ChmeetingService {
        var intercepter = Interceptor { chain ->
            var request = chain.request()
            var chmeetingApiKey = encryptedPrefs.getString(PREFS_CHMEETING_API_KEY, "")

            var newRequest = request.newBuilder()
                .addHeader("Accept", "application/octet-stream")
                .addHeader("ApiKey", chmeetingApiKey!!)
                .build()

            chain.proceed(newRequest)
        }

        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder().addInterceptor(intercepter)
                    .addInterceptor(logInterceptor)
                    .build()
            )
            .build()
            .create(ChmeetingService::class.java)
    }
}