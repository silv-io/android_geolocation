package at.tuwien.android_geolocation.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.telephony.*
import android.util.Log
import androidx.core.content.ContextCompat
import at.tuwien.android_geolocation.service.mls.CellTowerInfo
import at.tuwien.android_geolocation.service.mls.MLSRequest
import at.tuwien.android_geolocation.service.mls.WifiAccessPointInfo
import at.tuwien.android_geolocation.service.model.Position
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MozillaLocationService : Service() {

    private val mozillaLocationBinder = MozillaLocationBinder()

    companion object const {
        private const val oneMinuteInNanos = 6 * 10e9
    }

    inner class MozillaLocationBinder : Binder() {
        fun getService(): MozillaLocationService {
            return this@MozillaLocationService
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return mozillaLocationBinder
    }

    fun getMLSInfo(): MLSRequest? {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED
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

    suspend fun getGPSInfo(): Position? {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MozillaLocationService", "Permission for GPS missing")
            return null
        } else {
            val locationManager: LocationManager =
                this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d("MozillaLocationService", "GPS is not enabled")
                return null
            }
            val location =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.takeUnless {
                    // age is greater than one minute
                    (SystemClock.elapsedRealtimeNanos() - it.elapsedRealtimeNanos) > oneMinuteInNanos
                }
                    ?: suspendCancellableCoroutine<Location?> { continuation ->
                        locationManager.requestSingleUpdate(
                            LocationManager.GPS_PROVIDER,
                            object : LocationListener {
                                override fun onLocationChanged(location: Location?) {
                                    continuation.resume(location)
                                }

                                override fun onStatusChanged(
                                    provider: String?,
                                    status: Int,
                                    extras: Bundle?
                                ) {
                                    Log.d(
                                        "MozillaLocationService",
                                        "LocationListener Status changed"
                                    )
                                }

                                override fun onProviderEnabled(provider: String?) {
                                    Log.d("MozillaLocationService", "Provider enabled")
                                }

                                override fun onProviderDisabled(provider: String?) {
                                    continuation.resumeWithException(Exception("Provider was disabled"))
                                }

                            },
                            null
                        )
                    }

            Log.d("MozillaLocationService", "LocationManager got $location")

            return if (location != null) {
                Position(location.longitude, location.latitude, location.accuracy.toDouble())
            } else null
        }

    }

    private fun getTowerInfo(cellinfo: List<CellInfo>): List<CellTowerInfo> {
        return cellinfo.map {
            CellTowerInfo().apply {
                radioType = when (it) {
                    is CellInfoWcdma -> "wcdma"
                    is CellInfoLte -> "lte"
                    is CellInfoGsm -> "gsm"
                    else -> null
                }
                mobileCountryCode = when (it) {
                    is CellInfoWcdma -> it.cellIdentity.mcc
                    is CellInfoLte -> it.cellIdentity.mcc
                    is CellInfoGsm -> it.cellIdentity.mcc
                    else -> null
                }
                mobileNetworkCode = when (it) {
                    is CellInfoWcdma -> it.cellIdentity.mnc
                    is CellInfoLte -> it.cellIdentity.mnc
                    is CellInfoGsm -> it.cellIdentity.mnc
                    else -> null
                }
                locationAreaCode = when (it) {
                    is CellInfoWcdma -> it.cellIdentity.lac
                    is CellInfoLte -> it.cellIdentity.tac
                    is CellInfoGsm -> it.cellIdentity.lac
                    else -> null
                }
                cellId = when (it) {
                    is CellInfoWcdma -> it.cellIdentity.cid
                    is CellInfoLte -> it.cellIdentity.ci
                    is CellInfoGsm -> it.cellIdentity.cid
                    else -> null
                }

                age = when (it) {
                    is CellInfoWcdma -> System.nanoTime() / 1_000_000 - it.timeStamp / 1_000_000
                    is CellInfoLte -> System.nanoTime() / 1_000_000 - it.timeStamp / 1_000_000
                    is CellInfoGsm -> System.nanoTime() / 1_000_000 - it.timeStamp / 1_000_000
                    else -> null
                }
                //Cell Tower age cannot be read by using SDK
                //Cell Tower age out of scope

                // GSM has no psc
                psc = when (it) {
                    is CellInfoWcdma -> it.cellIdentity.psc
                    is CellInfoLte -> it.cellIdentity.pci
                    else -> null
                }
                signalStrength = when (it) {
                    is CellInfoWcdma -> it.cellSignalStrength.asuLevel
                    is CellInfoLte -> it.cellSignalStrength.asuLevel
                    is CellInfoGsm -> it.cellSignalStrength.asuLevel
                    else -> null
                }
                timingAdvance = when (it) {
                    is CellInfoLte -> it.cellSignalStrength.timingAdvance
                    else -> null
                }
            }
        }
    }

    private fun getWifiAccessPointInfo(wifiInfo: List<ScanResult>): List<WifiAccessPointInfo> {
        return wifiInfo.map {
            WifiAccessPointInfo().apply {
                macAddress = it.BSSID
                age = SystemClock.elapsedRealtimeNanos() / 1_000_000 - it.timestamp / 1_000
                channel = it.channelWidth
                frequency = it.frequency
                signalStrength = it.level
                // Noise Level ist not available when using SDK
                // Detection of noise level would require low-level NDK operations
                // Noise Level out of scope
                // newWifiAccessPoint.signalToNoiseRatio
                ssid = it.SSID
            }
        }
    }

}
