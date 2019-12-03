package at.tuwien.android_geolocation.service.model

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.math.abs

@Entity(tableName = "locations")
@TypeConverters(Converters::class)
data class Location(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @Embedded(prefix = "gps_") val gps: Position,
    @Embedded(prefix = "mls_") val mls: Position,
    @ColumnInfo(name = "captureTime") val captureTime: Date,
    @ColumnInfo(name = "params") val params: Map<String, String>
)

data class Position(
    val longitude: Double,
    val latitude: Double,
    val accuracy: Double
) {

    fun diff(p: Position): Position {
        return Position(
            p.longitude - this.longitude,
            p.latitude - this.latitude,
            abs(p.accuracy - this.accuracy)
        )
    }

    override fun toString(): String {
        return "long: ${this.longitude}; lat: ${this.latitude}"
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
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
}
