package at.tuwien.android_geolocation.service.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {

    /**
     * Select all tasks from the tasks table.
     *
     * @return all locations.
     */
    @Query("SELECT * FROM locations")
    suspend fun getLocations(): List<Location>

    /**
     * Select a location by id.
     *
     * @param locationId the location id.
     * @return the location with locationId.
     */
    @Query("SELECT * FROM locations WHERE id = :locationId")
    suspend fun getLocationById(locationId: Long): Location?

    /**
     * Insert a location in the database. If the location already exists, replace it.
     *
     * @param task the task to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location): Long

    /**
     * Delete a location by id.
     *
     * @return the number of locations deleted. This should always be 1.
     */
    @Query("DELETE FROM locations WHERE id = :locationId")
    suspend fun deleteLocationById(locationId: Long): Int

    /**
     * Delete all tasks.
     */
    @Query("DELETE FROM locations")
    suspend fun deleteLocations()
}