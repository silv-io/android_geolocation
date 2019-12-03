package at.tuwien.android_geolocation.service.model

import java.util.*
import kotlin.math.abs

data class Location(
    val id: Int,
    val gps: Position,
    val mls: Position,
    val captureTime: Date,
    val params: Map<String, String>
)

data class Position(val longitude: Double, val latitude: Double, val accuracy: Double) {

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

