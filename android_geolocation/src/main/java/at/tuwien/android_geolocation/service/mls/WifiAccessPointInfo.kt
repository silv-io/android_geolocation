package at.tuwien.android_geolocation.service.mls

data class WifiAccessPointInfo (
    var age: Long? = null,
    var channel: Int? = null,
    var frequency: Int? = null,
    var macAddress: String? = null,
    var signalStrength: Int? = null,
    var signalToNoiseRatio: Int? = null,
    var ssid: String? = null
)