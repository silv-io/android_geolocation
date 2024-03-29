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
import at.tuwien.android_geolocation.util.GpsProviderException
import at.tuwien.android_geolocation.util.MissingPermissionException
import at.tuwien.android_geolocation.util.NetworkProviderException
import at.tuwien.android_geolocation.util.NoCellTowerOrWifiInfoFound
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AntennaService : Service() {

    private val antennaBinder = AntennaBinder()

    companion object const {
        private const val oneMinuteInNanos = 6 * 10e9
    }

    inner class AntennaBinder : Binder() {
        fun getService(): AntennaService {
            return this@AntennaService
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return antennaBinder
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
            throw MissingPermissionException("Permission for Location or WifiStatus missing")
        } else {
            val locationManager: LocationManager =
                this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                throw NetworkProviderException("Location not enabled")
            }

            val telephonyManager: TelephonyManager =
                this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val wifiManager: WifiManager =
                this.getSystemService(Context.WIFI_SERVICE) as WifiManager
            var cellTowers: List<CellTowerInfo?>? = null
            var wifiAccessPoints: List<WifiAccessPointInfo?>? = null

            if (telephonyManager.networkType != TelephonyManager.NETWORK_TYPE_UNKNOWN) {
                cellTowers = getTowerInfo(telephonyManager.allCellInfo)
            }

            if (wifiManager.connectionInfo.networkId != -1) {
                wifiAccessPoints = getWifiAccessPointInfo(wifiManager.scanResults)
            }

            /*no need to send a MLS-request if there is no
                  celltower of wifiaccesspoint information*/
            if (cellTowers.isNullOrEmpty() && wifiAccessPoints.isNullOrEmpty()){
                throw NoCellTowerOrWifiInfoFound("No CellTower of Wifi-Accesspoint info could be found")
            }

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
            Log.d("AntennaService", "Permission for GPS missing")
            throw MissingPermissionException("Permission for Gps is missing")
        } else {
            val locationManager: LocationManager =
                this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d("AntennaService", "GPS is not enabled")
                throw GpsProviderException("GPS is not enabled")
            }

            try {
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
                                            "AntennaService",
                                            "LocationListener Status changed"
                                        )
                                    }

                                    override fun onProviderEnabled(provider: String?) {
                                        Log.d("AntennaService", "Provider enabled")
                                    }

                                    override fun onProviderDisabled(provider: String?) {
                                        Log.d("AntennaService", "Provider is disabled")
                                        continuation.cancel(Exception("Provider was disabled"))
                                    }
                                },
                                null
                            )
                        }

                Log.d("AntennaService", "LocationManager got $location")

                return if (location != null) {
                Position(location.longitude, location.latitude, location.accuracy.toDouble())
                } else null
            } catch (e: Exception) {
                Log.d("AntennaService", "Caught Exception: " + e.message)
                return null
            }
        }

    }

    private fun getTowerInfo(cellinfo: List<CellInfo?>): List<CellTowerInfo?> {
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

    private fun getWifiAccessPointInfo(wifiInfo: List<ScanResult?>): List<WifiAccessPointInfo> {
        return wifiInfo.map {
            WifiAccessPointInfo().apply {
                macAddress = it?.BSSID
                age = it?.let{SystemClock.elapsedRealtimeNanos() / 1_000_000 - it.timestamp / 1000}
                channel = it?.channelWidth
                frequency = it?.frequency
                signalStrength = it?.level
                // Noise Level ist not available when using SDK
                // Detection of noise level would require low-level NDK operations
                // Noise Level out of scope
                // newWifiAccessPoint.signalToNoiseRatio
                ssid = it?.SSID
            }
        }
    }

}
