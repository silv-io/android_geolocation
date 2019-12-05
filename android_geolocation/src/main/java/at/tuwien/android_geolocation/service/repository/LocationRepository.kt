package at.tuwien.android_geolocation.service.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import at.tuwien.android_geolocation.service.MozillaLocationService
import at.tuwien.android_geolocation.service.model.Location
import at.tuwien.android_geolocation.service.model.LocationDao
import at.tuwien.android_geolocation.service.model.Position
import at.tuwien.android_geolocation.util.Result
import com.tuwien.geolocation_android.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class LocationRepository(
    private val locationDao: LocationDao,
    private val locationService: MozillaLocationService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

) {
    private var idCounter: Long = 1

    suspend fun getLocations(): Result<List<Location>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(locationDao.getLocations())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getLocation(locationId: Long): Result<Location> = withContext(ioDispatcher) {
        try {
            val task = locationDao.getLocationById(locationId)
            if (task != null) {
                return@withContext Result.Success(task)
            } else {
                return@withContext Result.Error(Exception("Task not found!"))
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    suspend fun newLocation(): Result<Long> = withContext(ioDispatcher) {
        val location = Location(
            0L,
            Position(1.2, 2.3, 4.5),
            Position(6.7, 8.9, 0.1),
            DateTime.now(),
            HashMap<String, String>()
        )
        val locationId = locationDao.insertLocation(location)
        Log.println(Log.INFO,"location_repository", "got locationID: $locationId")
        return@withContext Result.Success(locationId)
    }

    suspend fun deleteAllLocations() = withContext(ioDispatcher) {
        locationDao.deleteLocations()
    }

    suspend fun deleteLocation(locationId: Long) = withContext<Unit>(ioDispatcher) {
        locationDao.deleteLocationById(locationId)
    }

    suspend fun createTemporaryLocationPlaintextFile(context: Context, text: String): Uri {
        val file = File(context.cacheDir, "location.txt")

        val fos = FileOutputStream(file)
        val osw = OutputStreamWriter(fos)
        osw.write(text)
        osw.close()
        fos.close()

        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
    }
}