package ca.trinityonhale.donationkiosk.module.app

import android.content.Context
import ca.trinityonhale.donationkiosk.DonationKioskApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): DonationKioskApplication {
        return app as DonationKioskApplication
    }
}