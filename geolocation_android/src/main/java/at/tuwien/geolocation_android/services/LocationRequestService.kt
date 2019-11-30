package at.tuwien.geolocation_android.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.*
import android.util.Log
import androidx.core.content.ContextCompat
import at.tuwien.geolocation_android.DataObjects.CellTower
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import com.android.volley.toolbox.Volley

class LocationRequestService {

    fun getMLSInfo(context: Context, activity: Activity) {
        val queue = Volley.newRequestQueue(context)

        //URL to the Mozilla Location Service API with the key "test". (Key can be changed)
        val url = "https://location.services.mozilla.com/v1/geolocate?key=test"

        val manager: TelephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            Log.d(
                "ACCESS_COARSE_LOCATION_PERMISSION_DENIED",
                "ACCESS_COARSE_LOCATION_PERMISSION_DENIED"
            )

        } else {

            val celltowers = getTowerInfo(manager.allCellInfo)
            Log.d("CELLTOWERS_INFO_FOR_JSON", celltowers.toString())

            //TODO: convert celltower info into JSON and get WIFI-AccessPointFields


        }


        //hardcoded dummy request
        val jsonrequest = JSONObject(
            "{\n" +
                    "    \"wifiAccessPoints\": [{\n" +
                    "        \"macAddress\": \"01:23:45:67:89:ab\",\n" +
                    "        \"signalStrength\": -51\n" +
                    "    }, {\n" +
                    "        \"macAddress\": \"01:23:45:67:89:cd\"\n" +
                    "    }]\n" +
                    "}"
        )

        val jsonRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonrequest,
            Response.Listener<JSONObject> { response ->
                Log.d(
                    "Response",
                    response.toString()
                )
            },
            Response.ErrorListener { Log.d("Error", "error") })

        /*
            // Request a string response from the provided URL.
        val stringRequest = StringRequest(
        Request.Method.POST, url,
        Response.Listener<String> { response ->
            // Display the first 500 characters of the response string.
            textView.text = "Response is: ${response.substring(0, 500)}"
        },
        Response.ErrorListener { textView.text = "That didn't work!" })
        */

        // Add the request to the RequestQueue.
        queue.add(jsonRequest)

    }


    private fun getTowerInfo(cellinfo: List<CellInfo>): MutableList<CellTower> {

        Log.d("CELLINFO", cellinfo.toString())
        val celltowers: MutableList<CellTower> = mutableListOf<CellTower>()
        for (cell_info_entry in cellinfo) {
            /*if (cell_info_entry is CellInfoWcdma){
                celltowers+="wcda"
                val cellinfowcdma = cell_info_entry as CellInfoWcdma
                cellinfowcdma.cellIdentity.mcc
            }
            cell_info_entry*/
            val newTower = CellTower()
            //val cellinfowcdma = cell_info_entry as CellInfoWcdma
            //cellinfowcdma.cellSignalStrength.

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
            newTower.psc = when (cell_info_entry) {
                is CellInfoWcdma -> cell_info_entry.cellIdentity.psc
                is CellInfoLte -> cell_info_entry.cellIdentity.pci
                is CellInfoGsm -> cell_info_entry.cellIdentity.psc
                else -> null
            }
            newTower.signalstrength = when (cell_info_entry) {
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
}