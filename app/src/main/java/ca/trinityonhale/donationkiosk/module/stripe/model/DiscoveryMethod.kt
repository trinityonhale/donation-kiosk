package ca.trinityonhale.donationkiosk.module.stripe.model

enum class DiscoveryMethod(
    val value: String
) {

    BLUETOOTH_SCAN("bluetooth_scan"),
//    INTERNET("internet"),
    USB("usb");

    companion object {
        fun fromValue(value: String): DiscoveryMethod {
            return when (value) {
                "bluetooth_scan" -> BLUETOOTH_SCAN
//                "internet" -> INTERNET
                "usb" -> USB
                else -> throw IllegalArgumentException("Invalid value for DiscoveryMethod")
            }
        }
    }
}
