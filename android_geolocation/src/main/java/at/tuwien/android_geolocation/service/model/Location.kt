package at.tuwien.android_geolocation.service.model

import androidx.room.*
import at.tuwien.android_geolocation.service.mls.MLSRequest
import at.tuwien.android_geolocation.util.round
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
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
    companion object {
        val pattern: DateTimeFormatter = DateTimeFormat.forPattern("d.M.y H:m:s")
    }

    fun getFormattedTimestamp(): String {
        return captureTime.toString(pattern)
    }

    val plaintextString: String
        get() = """Timestamp: ${getFormattedTimestamp()}${System.lineSeparator()}
            |MLS:  ${this.mls}${System.lineSeparator()}
            |GPS:  ${this.gps}${System.lineSeparator()}
            |MLS parameters: ${this.params}
        """.trimMargin()

}

data class Position(
    val longitude: Double,
    val latitude: Double,
    val accuracy: Double
) {

    companion object {
        const val earthRadiusInMeters: Double = 6_372_800.0
    }

    val roundAccuracy: Double
        get() = this.accuracy.round(2)

    /**
     * Haversine formula
     * @return Distance in Meters rounded to 2 decimals
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
        return (earthRadiusInMeters * c).round(2)
    }

    override fun toString(): String {
        return String.format(
            "%s%c %s%c",
            degToCoordString(latitude),
            if (latitude >= 0) 'N' else 'S',
            degToCoordString(longitude),
            if (longitude >= 0) 'E' else 'W'
        )
    }

    private fun degToCoordString(fullDeg: Double): String {
        var conversionVar = fullDeg

        val deg: Int = floor(conversionVar).toInt()
        conversionVar = (conversionVar - deg) * 60
        val arcmin: Int = floor(conversionVar).toInt()
        val arcsec: Double = (conversionVar - arcmin) * 60

        return String.format("%d°%d'%.2f''", deg, arcmin, arcsec)
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
    fun fromMLSRequestToString(request: MLSRequest): String {
        val gson = Gson()
        return gson.toJson(request)
    }

    @TypeConverter
    fun fromStringToMLSRequest(value: String): MLSRequest {
        val mlsRequestType = object : TypeToken<MLSRequest>() {}.type
        return Gson().fromJson(value, mlsRequestType)
    }
}
