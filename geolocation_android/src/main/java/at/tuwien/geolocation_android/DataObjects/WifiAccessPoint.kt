package at.tuwien.geolocation_android.DataObjects


data class WifiAccessPoint(
    var macAddress: String? = null,
    var age: Int? = null,
    var channel: Int? = null,
    var frequency: Int? = null,
    var signalStrength: Int? = null,
    var signalToNoiseRatio: Int? = null,
    var ssid: String? = null
) {
}