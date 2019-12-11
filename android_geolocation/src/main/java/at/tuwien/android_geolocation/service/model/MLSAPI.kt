package at.tuwien.android_geolocation.service.model

import at.tuwien.android_geolocation.service.mls.MLSRequest
import at.tuwien.android_geolocation.service.mls.MLSResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/*
 * Our solution for having the API-key in a secure place would be to have the API-call below on a
 * dedicated server which serves as a proxy. Because we don't have the infrastructure to do so,
 * we call it here directly, because the response from the proxy would still be the same as the
 * real API call. The main difference would be that we would need some sort of credential-system
 * to access the proxy resource, which could e.g. be done by having account management.
 */
interface MLSAPI {
    @POST("geolocate?key=test")
    fun getMLSLocation(@Body mlsRequest: MLSRequest?): Call<MLSResponse?>?

    @POST("geolocate?key=test")
    fun getMLSLocationSecure(@Body mlsRequest: MLSRequest?): Call<MLSResponse?>?
}