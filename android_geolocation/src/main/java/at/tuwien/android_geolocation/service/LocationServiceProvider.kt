package at.tuwien.android_geolocation.service

import android.content.Context
import androidx.room.Room
import at.tuwien.android_geolocation.service.repository.LocationRepository
import com.commonsware.cwac.saferoom.SQLCipherUtils

object LocationServiceProvider {

    private var db: LocationDb? = null
    @Volatile
    var locationRepository: LocationRepository? = null

    fun provideRepository(context: Context): LocationRepository {
        synchronized(this) {
            return locationRepository ?: locationRepository ?: createRepository(context)
        }
    }

    private fun createRepository(context: Context): LocationRepository {
        val db = db ?: createDb(context)
        //TODO: implement MozillaLocationService
        return LocationRepository(db.locationDao(), MozillaLocationService())
    }

    private fun createDb(context: Context): LocationDb {
        val result = Room.databaseBuilder(
            context.applicationContext,
            LocationDb::class.java, "Locations.db"
        ).build()
        db = result
        return result
    }

    fun encryptDb(context: Context, passphrase: ByteArray): Boolean {
        return when (SQLCipherUtils.getDatabaseState(context.applicationContext, "Locations.db")) {
            SQLCipherUtils.State.UNENCRYPTED -> {
                db!!.close()
                SQLCipherUtils.encrypt(context.applicationContext, "Locations.db", passphrase)
                db!!.isOpen
            }
            else -> {
                false
            }
        }
    }
}