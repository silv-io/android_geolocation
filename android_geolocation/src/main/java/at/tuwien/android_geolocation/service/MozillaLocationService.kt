package at.tuwien.android_geolocation.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.IBinder
import android.telephony.*
import android.util.Log
import androidx.core.content.ContextCompat
import at.tuwien.android_geolocation.service.mls.CellTowerInfo
import at.tuwien.android_geolocation.service.mls.MLSRequest
import at.tuwien.android_geolocation.service.mls.WifiAccessPointInfo
import at.tuwien.android_geolocation.service.model.Position

class MozillaLocationService : Service() {

    private val mozillaLocationBinder = MozillaLocationBinder()

    inner class MozillaLocationBinder : Binder() {
        fun getService(): MozillaLocationService {
            return this@MozillaLocationService
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return mozillaLocationBinder
    }

    fun getMLSInfo(): MLSRequest? {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        } else {
            val telephonyManager: TelephonyManager =
                this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            val wifiManager: WifiManager =
                this.getSystemService(Context.WIFI_SERVICE) as WifiManager

            val cellTowers = getTowerInfo(telephonyManager.allCellInfo)
            val wifiAccessPoints = getWifiAccessPointInfo(wifiManager.scanResults)

            return MLSRequest(
                cellTowers = cellTowers, wifiAccessPoints = wifiAccessPoints,
                considerIp = null, carrier = null,
                homeMobileCountryCode = null, homeMobileNetworkCode = null
            )
        }
    }

    fun getGPSInfo(): Position? {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        } else {
            val locationManager: LocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                return Position(location.longitude, location.latitude, location.accuracy.toDouble())
            }
        }

        return null
    }

    private fun getTowerInfo(cellinfo: List<CellInfo>): MutableList<CellTowerInfo> {
        val celltowers: MutableList<CellTowerInfo> = mutableListOf<CellTowerInfo>()
        for (cell_info_entry in cellinfo) {
            val newTower = CellTowerInfo()

            newTower.radioType = when (cell_info_entry) {
                is CellInfoWcdma -> "wcdma"
                is CellInfoLte -> "lte"
                is CellInfoGsm -> "gsm"
                else -> null
            }
            newTower.mobileCountryCode = when (cell_info_entry) {
                is CellInfoWcdma -> cell_info_entry.cellIdentity.mcc
                is CellInfoLte -> cell_info_entry.cellIdentity.mcc
                is CellInfoGsm -> cell_info_entry.cellIdentity.mcc
                else -> null
            }
            newTower.mobileNetworkCode = when (cell_info_entry) {
                is CellInfoWcdma -> cell_info_entry.cellIdentity.mnc
                is CellInfoLte -> cell_info_entry.cellIdentity.mnc
                is CellInfoGsm -> cell_info_entry.cellIdentity.mnc
                else -> null
            }
            newTower.locationAreaCode = when (cell_info_entry) {
                is CellInfoWcdma -> cell_info_entry.cellIdentity.lac
                is CellInfoLte -> cell_info_entry.cellIdentity.tac
                is CellInfoGsm -> cell_info_entry.cellIdentity.lac
                else -> null
            }
            newTower.cellId = when (cell_info_entry) {
                is CellInfoWcdma -> cell_info_entry.cellIdentity.cid
                is CellInfoLte -> cell_info_entry.cellIdentity.ci
                is CellInfoGsm -> cell_info_entry.cellIdentity.cid
                else -> null
            }
            //Cell Tower age cannot be read by using SDK
            //Cell Tower age out of scope
            newTower.psc = when (cell_info_entry) {
                is CellInfoWcdma -> cell_info_entry.cellIdentity.psc
                is CellInfoLte -> cell_info_entry.cellIdentity.pci
                is CellInfoGsm -> cell_info_entry.cellIdentity.psc
                else -> null
            }
            newTower.signalStrength = when (cell_info_entry) {
                is CellInfoWcdma -> cell_info_entry.cellSignalStrength.asuLevel
                is CellInfoLte -> cell_info_entry.cellSignalStrength.asuLevel
                is CellInfoGsm -> cell_info_entry.cellSignalStrength.asuLevel
                else -> null
            }
            newTower.timingAdvance = when (cell_info_entry) {
                is CellInfoLte -> cell_info_entry.cellSignalStrength.timingAdvance
                else -> null
            }
            celltowers.add(newTower)
        }
        return celltowers
    }

    private fun getWifiAccessPointInfo(wifiInfo: List<ScanResult>): MutableList<WifiAccessPointInfo> {
        val wifiAccessPoints: MutableList<WifiAccessPointInfo> =
            mutableListOf<WifiAccessPointInfo>()
        for (wifi_info_entry in wifiInfo) {
            if (!wifi_info_entry.SSID.contains("_nomap")) {
                val newWifiAccessPoint = WifiAccessPointInfo()
                newWifiAccessPoint.macAddress = wifi_info_entry.BSSID
                newWifiAccessPoint.age = wifi_info_entry.timestamp
                newWifiAccessPoint.channel = wifi_info_entry.channelWidth
                newWifiAccessPoint.frequency = wifi_info_entry.frequency
                newWifiAccessPoint.signalStrength = wifi_info_entry.level
                // Noise Level ist not available when using SDK
                // Detection of noise level would require low-level NDK operations
                // Noise Level out of scope
                // newWifiAccessPoint.signalToNoiseRatio
                newWifiAccessPoint.ssid = wifi_info_entry.SSID
                wifiAccessPoints.add(newWifiAccessPoint)
            }
        }
        return wifiAccessPoints
    }

}
