package at.tuwien.android_geolocation.service.networking

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MLSRetrofitService {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://location.services.mozilla.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <S> createService(serviceClass: Class<S>): S {
        return retrofit.create(serviceClass)
    }
}