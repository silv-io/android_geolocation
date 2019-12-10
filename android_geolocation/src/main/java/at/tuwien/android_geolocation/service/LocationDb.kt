package at.tuwien.android_geolocation.service

import androidx.room.Database
import androidx.room.RoomDatabase
import at.tuwien.android_geolocation.service.model.Location
import at.tuwien.android_geolocation.service.model.LocationDao

/**
 * The Room Database that contains the Location table.
 *
 * Note that exportSchema should be true in production databases.
 */
@Database(entities = [Location::class], version = 1, exportSchema = false)
abstract class LocationDb : RoomDatabase() {
    abstract fun locationDao(): LocationDao

}