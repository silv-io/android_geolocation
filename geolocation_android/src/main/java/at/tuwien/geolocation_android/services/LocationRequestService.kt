package at.tuwien.geolocation_android.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.CellInfo
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
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

            //val cellinfo: MutableList<CellInfo> = manager.allCellInfo
            val cellinfo: List<CellInfo> = manager.allCellInfo


            //var celltowers: String

            /*for (cell_info_entry in cellinfo) {

            }*/

            Log.d("CELLINFO", cellinfo.toString())

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

            val stringRequest = JsonObjectRequest(
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
            queue.add(stringRequest)

        }
    }
}