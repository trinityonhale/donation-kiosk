package ca.trinityonhale.donationkiosk.module.encryptedprefs

import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore

class SharedPreferenceDataStore(
    private var sharedPreferences: SharedPreferences
): PreferenceDataStore() {

    override fun putString(key: String?, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getString(key: String?, defValue: String?): String? {
        return sharedPreferences.getString(key, defValue)
    }

    override fun putStringSet(key: String?, values: MutableSet<String>?) {
        sharedPreferences.edit().putStringSet(key, values).apply()
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        return sharedPreferences.getStringSet(key, defValues)
    }

    override fun putInt(key: String?, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return sharedPreferences.getInt(key, defValue)
    }

    override fun putLong(key: String?, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return sharedPreferences.getLong(key, defValue)
    }

    override fun putFloat(key: String?, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return sharedPreferences.getFloat(key, defValue)
    }

    override fun putBoolean(key: String?, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defValue)
    }
}