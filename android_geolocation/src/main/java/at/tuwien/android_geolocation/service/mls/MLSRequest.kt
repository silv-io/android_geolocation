package at.tuwien.android_geolocation.service.mls

import com.google.gson.GsonBuilder

data class MLSRequest(
    val carrier: String?,
    val cellTowers: List<CellTowerInfo>,
    val considerIp: Boolean?,
    val homeMobileCountryCode: Int?,
    val homeMobileNetworkCode: Int?,
    val wifiAccessPoints: List<WifiAccessPointInfo>
) {
    override fun toString(): String {
        return GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(this).toString()
    }
}