package ca.trinityonhale.donationkiosk

import android.app.Application
import android.os.StrictMode
import com.stripe.stripeterminal.TerminalApplicationDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DonationKioskApplication : Application() {

    override fun onCreate() {
//        StrictMode.setThreadPolicy(
//            StrictMode.ThreadPolicy.Builder()
//                .detectDiskReads()
//                .detectDiskWrites()
//                .detectAll()
//                .penaltyLog()
//                .build()
//        )
//
//        StrictMode.setVmPolicy(
//            StrictMode.VmPolicy.Builder()
//                .detectLeakedSqlLiteObjects()
//                .detectLeakedClosableObjects()
//                .penaltyLog()
//                .build()
//        )

        super.onCreate()
        TerminalApplicationDelegate.onCreate(this)
    }
}