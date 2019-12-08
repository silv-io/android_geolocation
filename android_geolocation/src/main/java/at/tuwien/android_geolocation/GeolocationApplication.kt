package at.tuwien.android_geolocation

import android.app.Application
import net.danlew.android.joda.JodaTimeAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class GeolocationApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@GeolocationApplication)
            modules(
                listOf(
                    repositoryModule,
                    viewModelModule,
                    databaseModule,
                    retrofitModule,
                    apiModule
                )
            )
        }
    }

}