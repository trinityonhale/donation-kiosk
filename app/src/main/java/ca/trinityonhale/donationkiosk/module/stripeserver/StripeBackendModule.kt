package ca.trinityonhale.donationkiosk.module.stripeserver

import android.content.SharedPreferences
import ca.trinityonhale.donationkiosk.module.encryptedprefs.EncryptedPrefsModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(
    includes = [
        EncryptedPrefsModule::class
    ]
)
@InstallIn(SingletonComponent::class)
object StripeBackendModule {

    @Provides
    @Singleton
    fun provideStripeBackendService(encryptedPrefs: SharedPreferences): StripeBackendService {
        return StripeBackendService(encryptedPrefs)
    }
}