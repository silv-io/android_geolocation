package at.tuwien.android_geolocation

import android.app.Application
import android.content.Context
import androidx.room.Room
import at.tuwien.android_geolocation.service.LocationDb
import at.tuwien.android_geolocation.service.model.LocationDao
import at.tuwien.android_geolocation.service.model.MLSAPI
import at.tuwien.android_geolocation.service.repository.LocationRepository
import at.tuwien.android_geolocation.viewmodel.location.LocationDetailsViewModel
import at.tuwien.android_geolocation.viewmodel.location.LocationListViewModel
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tuwien.geolocation_android.R
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val viewModelModule = module {
    viewModel { LocationListViewModel(get(), get()) }
    viewModel { LocationDetailsViewModel(get(), get()) }
}

val repositoryModule = module {
    fun provideUserRepository(dao: LocationDao, api: MLSAPI, application: Application): LocationRepository {
        return LocationRepository(dao, api, application.applicationContext)
    }

    single { provideUserRepository(get(), get(), get()) }
}

val databaseModule = module {
    fun provideDatabase(application: Application): LocationDb {
        return Room.databaseBuilder(
            application.applicationContext,
            LocationDb::class.java, application.applicationContext.getString(R.string.database_name)
        )
            .allowMainThreadQueries()
            .build()
    }


    fun provideDao(database: LocationDb): LocationDao {
        return database.locationDao()
    }

    single { provideDatabase(androidApplication()) }
    single { provideDao(get()) }
}

val apiModule = module {
    fun provideUserApi(retrofit: Retrofit): MLSAPI {
        return retrofit.create(MLSAPI::class.java)
    }

    single { provideUserApi(get()) }
}

val retrofitModule = module {
    fun provideCache(application: Application): Cache {
        val cacheSize = 10 * 1024 * 1024
        return Cache(application.cacheDir, cacheSize.toLong())
    }

    fun provideHttpClient(cache: Cache): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .cache(cache)

        return okHttpClientBuilder.build()
    }

    fun provideGson(): Gson {
        return GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create()
    }


    fun provideRetrofit(factory: Gson, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://location.services.mozilla.com/v1/")
            .addConverterFactory(GsonConverterFactory.create(factory))
            .client(client)
            .build()
    }

    single { provideCache(androidApplication()) }
    single { provideHttpClient(get()) }
    single { provideGson() }
    single { provideRetrofit(get(), get()) }
}