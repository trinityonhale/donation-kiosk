package ca.trinityonhale.donationkiosk

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

open class FullScreenAppCompatActivity: AppCompatActivity() {

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hideSystemUI_R()
        } else {
            hideSystemUI()
        }
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE
                        or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI_R() {
        window.decorView.windowInsetsController!!.hide(
            android.view.WindowInsets.Type.statusBars()
                    or android.view.WindowInsets.Type.navigationBars()
        )
        window.decorView.windowInsetsController!!.hide(
            android.view.WindowInsets.Type.statusBars()
        )
    }
}