package at.tuwien.geolocation_android.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.telephony.*
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import at.tuwien.geolocation_android.DataObjects.CellTower
import at.tuwien.geolocation_android.DataObjects.WifiAccessPoint
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.tuwien.geolocation_android.R
import kotlinx.android.synthetic.main.fragment_location_details.*

class LocationRequestService {

    fun getMLSInfo(context: Context, activity: Activity, textView: TextView) {
        val queue = Volley.newRequestQueue(context)

        //URL to the Mozilla Location Service API with the key "test". (Key can be changed)
        val url = "https://location.services.mozilla.com/v1/geolocate?key=test"

        val telephonyManager: TelephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val wifiManager: WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            Log.d(
                "PERMISSION_DENIED",
                "CHECK APP PERMISSIONS"
            )

        } else {

            val cellTowers = getTowerInfo(telephonyManager.allCellInfo)
            //Log.d("CELLTOWERS_INFO_FOR_JSON", cellTowers.toString())

            //Log.d("CONNECTION INFO ------------->", wifiManager.connectionInfo.toString())
            //Log.d("SCAN RESULTS ------------->", wifiManager.scanResults.toString())

            val wifiAccessPoints = getWifiAccessPointInfo(wifiManager.scanResults)


            val gson = GsonBuilder().setPrettyPrinting().create()
            val gsonTowers = gson.toJson(cellTowers)
            Log.d("JSON_OF_CELLTOWERS", gsonTowers)
            val gsonWifiAccessPoints = gson.toJson(wifiAccessPoints)
            Log.d("JSON_OF_WIFIACCESSPOINTS", gsonWifiAccessPoints)

            var requestObject: String = "{\n"
            if (cellTowers.isNotEmpty()) requestObject += "\"cellTowers\": " + gsonTowers
            //check for wifiaccesspoints here
            if (wifiAccessPoints.isNotEmpty()) requestObject += ",\n\"wifiAccessPoints\": " + gsonWifiAccessPoints
            requestObject += "\n}"



            val jsonrequest = JSONObject(
                requestObject
            )

            Log.d("JSONREQUEST", requestObject)

            val jsonRequest = JsonObjectRequest(
                Request.Method.POST, url, jsonrequest,
                Response.Listener<JSONObject> { response ->
                    textView.text=response.toString()
//                    Log.d(
//                        "Response",
//                        response.toString()
//                    )
                },
                Response.ErrorListener { Log.d("Error", "error") })

            // Add the request to the RequestQueue.
            queue.add(jsonRequest)
        }

    }


    private fun getTowerInfo(cellinfo: List<CellInfo>): MutableList<CellTower> {

        Log.d("CELLINFO", cellinfo.toString())
        val celltowers: MutableList<CellTower> = mutableListOf<CellTower>()
        for (cell_info_entry in cellinfo) {

            val newTower = CellTower()


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
            newTower.cellid = when (cell_info_entry) {
                is CellInfoWcdma -> cell_info_entry.cellIdentity.cid
                is CellInfoLte -> cell_info_entry.cellIdentity.ci
                is CellInfoGsm -> cell_info_entry.cellIdentity.cid
                else -> null
            }

            //newTower.age ...

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

    private fun getWifiAccessPointInfo(wifiInfo: List<ScanResult>): MutableList<WifiAccessPoint>{

        val wifiAccessPoints: MutableList<WifiAccessPoint> = mutableListOf<WifiAccessPoint>()
        for (wifi_info_entry in wifiInfo){
            if(!wifi_info_entry.SSID.contains("_nomap")) {
                val newWifiAccessPoint = WifiAccessPoint()
                newWifiAccessPoint.macAddress = wifi_info_entry.BSSID
                //newWifiAccessPoint.age = wifi_info_entry.timestamp as Int
                newWifiAccessPoint.channel = wifi_info_entry.channelWidth
                newWifiAccessPoint.frequency = wifi_info_entry.frequency
                newWifiAccessPoint.signalStrength = wifi_info_entry.level
                //newWifiAccessPoint.signalToNoiseRatio
                newWifiAccessPoint.ssid = wifi_info_entry.SSID
                wifiAccessPoints.add(newWifiAccessPoint)
            }
        }
        return wifiAccessPoints
    }
}