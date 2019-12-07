package at.tuwien.android_geolocation.service.mls

data class MLSRequest(
    val carrier: String?,
    val cellTowers: List<CellTowerInfo>,
    val considerIp: Boolean?,
    val homeMobileCountryCode: Int?,
    val homeMobileNetworkCode: Int?,
    val wifiAccessPoints: List<WifiAccessPointInfo>
)