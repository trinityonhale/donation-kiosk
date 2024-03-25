package ca.trinityonhale.donationkiosk.module.stripe

import ca.trinityonhale.donationkiosk.module.encryptedprefs.EncryptedPrefsModule
import ca.trinityonhale.donationkiosk.module.stripe.provider.TokenProvider
import ca.trinityonhale.donationkiosk.module.stripeserver.StripeBackendModule
import ca.trinityonhale.donationkiosk.module.stripeserver.StripeBackendService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(
    includes = [
        StripeBackendModule::class,
        EncryptedPrefsModule::class
    ]
)

@InstallIn(SingletonComponent::class)
object StripeModule {

    @Provides
    fun provideTokenProvider(stripeBackendService: StripeBackendService): TokenProvider {
        return TokenProvider(stripeBackendService)
    }
}