package at.tuwien.android_geolocation.service.model

import androidx.room.*
import at.tuwien.android_geolocation.service.mls.MLSRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import kotlin.math.*

@Entity(tableName = "locations")
@TypeConverters(Converters::class)
data class Location(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0L,
    @Embedded(prefix = "gps_") val gps: Position,
    @Embedded(prefix = "mls_") val mls: Position,
    @ColumnInfo(name = "captureTime") val captureTime: DateTime,
    @ColumnInfo(name = "params") val params: MLSRequest
) {
    fun toPlaintextString(): String {
        val builder: StringBuilder = StringBuilder()

        builder.append("Timestamp: " + this.captureTime + System.lineSeparator())
        builder.append("MLS: " + this.mls + System.lineSeparator())
        builder.append("GPS: " + this.gps + System.lineSeparator())
        builder.append("MLS parameters: " + this.params)

        return builder.toString()
    }
}

data class Position(
    val longitude: Double,
    val latitude: Double,
    val accuracy: Double
) {

    companion object {
        const val earthRadiusInMeters: Double = 6_372_800.0
    }

    /**
     * Haversine formula
     * @return Distance in Meters
     */
    fun diff(that: Position): Double {
        val latitudeDifference = Math.toRadians(that.latitude - this.latitude)
        val longitudeDifference = Math.toRadians(that.longitude - this.longitude)
        val thisLatitudeRadians = Math.toRadians(this.latitude)
        val thatLatitudeRadians = Math.toRadians(that.latitude)

        val a = sin(latitudeDifference / 2).pow(2.toDouble()) + sin(longitudeDifference / 2).pow(
            2.toDouble()
        ) * cos(thisLatitudeRadians) * cos(thatLatitudeRadians)
        val c = 2 * asin(sqrt(a))
        return earthRadiusInMeters * c
    }

    override fun toString(): String {
        return "long: ${this.longitude}; lat: ${this.latitude}"
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): DateTime? {
        return value?.let { DateTime(it) }
    }

    @TypeConverter
    fun dateTimeToTimestamp(dateTime: DateTime?): Long? {
        return dateTime?.millis
    }

    @TypeConverter
    fun fromString(value: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, String>): String {
        val gson = Gson()
        return gson.toJson(map)
    }

    @TypeConverter
    fun fromMLSRequestToString(request: MLSRequest): String {
        val gson = Gson()
        return gson.toJson(request)
    }

    @TypeConverter
    fun fromStringToMLSRequest(value: String): MLSRequest {
        val mlsRequestType = object : TypeToken<MLSRequest>(){}.type
        return Gson().fromJson(value, mlsRequestType)
    }
}
