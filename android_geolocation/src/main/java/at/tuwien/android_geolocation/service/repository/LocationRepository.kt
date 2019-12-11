package at.tuwien.android_geolocation.service.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.room.Room
import at.tuwien.android_geolocation.service.LocationDb
import at.tuwien.android_geolocation.service.mls.MLSRequest
import at.tuwien.android_geolocation.service.mls.MLSResponse
import at.tuwien.android_geolocation.service.model.Location
import at.tuwien.android_geolocation.service.model.LocationDao
import at.tuwien.android_geolocation.service.model.MLSAPI
import at.tuwien.android_geolocation.service.model.Position
import at.tuwien.android_geolocation.util.Result
import com.commonsware.cwac.saferoom.SQLCipherUtils
import com.commonsware.cwac.saferoom.SafeHelperFactory
import com.tuwien.geolocation_android.BuildConfig
import com.tuwien.geolocation_android.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter


class LocationRepository(
    private val locationDao: LocationDao,
    private val mlsAPI: MLSAPI,
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private var encryptedDatabase: LocationDb? = null

    suspend fun getLocations(): Result<List<Location>> = withContext(ioDispatcher) {
        return@withContext try {
            if (encryptedDatabase != null) {
                Result.Success(encryptedDatabase!!.locationDao().getLocations())
            } else {
                Result.Success(locationDao.getLocations())
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getLocation(locationId: Long): Result<Location> = withContext(ioDispatcher) {
        try {
            val location = if (encryptedDatabase != null) {
                encryptedDatabase!!.locationDao().getLocationById(locationId)
            } else {
                locationDao.getLocationById(locationId)
            }
            if (location != null) {
                return@withContext Result.Success(location)
            } else {
                return@withContext Result.Error(Exception("Task not found!"))
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    suspend fun newLocation(mlsRequest: MLSRequest, gps: Position?): Result<Long> =
        withContext(ioDispatcher) {

            val mlsPosition = getMLSPosition(mlsRequest) ?: return@withContext Result.Error(
                Exception("MLS query failed")
            )

            val location = Location(
                0L,
                mls = mlsPosition,
                gps = gps ?: return@withContext Result.Error(Exception("GPS failed")),
                captureTime = DateTime.now(),
                params = mlsRequest
            )

            val locationId = if (encryptedDatabase != null) {
                encryptedDatabase!!.locationDao().insertLocation(location)
            } else {
                locationDao.insertLocation(location)
            }

            Log.println(Log.INFO, "location_repository", "got locationID: $locationId")
            return@withContext Result.Success(locationId)
        }

    suspend fun deleteAllLocations() = withContext(ioDispatcher) {
        if (encryptedDatabase != null) {
            encryptedDatabase!!.locationDao().deleteLocations()
        } else {
            locationDao.deleteLocations()
        }
    }

    suspend fun deleteLocation(locationId: Long) = withContext<Unit>(ioDispatcher) {
        if (encryptedDatabase != null) {
            encryptedDatabase!!.locationDao().deleteLocationById(locationId)
        } else {
            locationDao.deleteLocationById(locationId)
        }
    }

    fun createTemporaryLocationPlaintextFile(text: String): Uri {
        val file = File(context.cacheDir, "location.txt")

        val fos = FileOutputStream(file)
        val osw = OutputStreamWriter(fos)
        osw.write(text)
        osw.close()
        fos.close()

        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
    }

    private fun getMLSPosition(mlsRequest: MLSRequest): Position? {
        val mlsResponse: MLSResponse?

        val response: Response<MLSResponse?>? = mlsAPI.getMLSLocation(mlsRequest)?.execute()

        return if (response != null && response.isSuccessful) {
            mlsResponse = response.body()
            val mlsResponseCopy = mlsResponse!!.copy()
            validateAndConvertMLSResponse(mlsResponseCopy)
        } else {
            null
        }
    }

    private fun validateAndConvertMLSResponse(mlsResponse: MLSResponse): Position {
        if (mlsResponse.error != null) {
            Log.println(
                Log.ERROR,
                "Error in MLS response",
                "MLS returned an error " + mlsResponse.error.code
            )
            Log.println(Log.ERROR, "Error in MLS response", mlsResponse.error.message!!)
        }

        return Position(
            longitude = mlsResponse.location?.lng!!,
            latitude = mlsResponse.location.lat!!,
            accuracy = mlsResponse.accuracy!!
        )
    }

    fun isDatabaseEncrypted(): Boolean {
        return SQLCipherUtils.getDatabaseState(context, context.getString(R.string.database_name)) == SQLCipherUtils.State.ENCRYPTED
    }

    fun openEncryptedDatabase(secret: ByteArray) {
        if (encryptedDatabase == null) {
            encryptedDatabase = buildEncryptedDatabase(context, secret)
        }

        // clear secret
        /* for (i in secret.indices) {
            secret[i] = 0.toByte()
        } */
    }

    fun closeEncryptedDatabase() {
        encryptedDatabase?.close()
    }

    private fun buildEncryptedDatabase(context: Context, secret: ByteArray): LocationDb {
        val factory = SafeHelperFactory(secret)

        if (SQLCipherUtils.getDatabaseState(context, context.getString(R.string.database_name)) == SQLCipherUtils.State.UNENCRYPTED) {
            Log.println(Log.ERROR, "######################", "encrypting database!")
            SQLCipherUtils.encrypt(context, context.getString(R.string.database_name), secret)
        } else if (SQLCipherUtils.getDatabaseState(context, context.getString(R.string.database_name)) == SQLCipherUtils.State.ENCRYPTED) {
            Log.println(Log.ERROR, "######################", "database is encrypted!")
        }

        val locationDb = Room.databaseBuilder(context, LocationDb::class.java, context.getString(R.string.database_name))
            .openHelperFactory(factory)
            .build()

        Log.println(Log.ERROR, "######################", locationDb.isOpen.toString())

        // clear secret
        /* for (i in secret.indices) {
            secret[i] = 0.toByte()
        } */

        return locationDb
    }
}