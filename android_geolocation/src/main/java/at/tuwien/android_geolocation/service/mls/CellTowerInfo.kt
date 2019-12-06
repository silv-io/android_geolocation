package at.tuwien.android_geolocation.service.mls

data class CellTowerInfo (
    var age: Long? = null,
    var cellId: Int? = null,
    var locationAreaCode: Int? = null,
    var mobileCountryCode: Int? = null,
    var mobileNetworkCode: Int? = null,
    var psc: Int? = null,
    var radioType: String? = null,
    var signalStrength: Int? = null,
    var timingAdvance: Int? = null
)