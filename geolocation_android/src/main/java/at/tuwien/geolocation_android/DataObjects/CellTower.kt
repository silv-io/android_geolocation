package at.tuwien.geolocation_android.DataObjects

data class CellTower(
    var radioType: String? = null, var mobileCountryCode: Int? = null,
    var mobileNetworkCode: Int? = null, var locationAreaCode: Int? = null,
    var cellid: Int? = null, var psc: Int? = null, var signalStrength: Int? = null,
    var timingAdvance: Int? = null
) {
}