package at.tuwien.android_geolocation.service.model

import at.tuwien.android_geolocation.service.mls.MLSRequest
import at.tuwien.android_geolocation.service.mls.MLSResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MLSAPI {
    @POST("geolocate?key=test")
    fun getMLSLocation(@Body mlsRequest: MLSRequest?): Call<MLSResponse?>?
}