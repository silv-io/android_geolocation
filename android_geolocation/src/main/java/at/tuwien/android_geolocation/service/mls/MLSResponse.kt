package at.tuwien.android_geolocation.service.mls

data class MLSResponse(
    val accuracy: Double?,
    val error: Error?,
    val fallback: String?,
    val location: Location?
)

data class Location(
    val lat: Double?,
    val lng: Double?
)

data class Error(
    val code: Int?,
    val errors: List<ErrorX>?,
    val message: String?
)

data class ErrorX(
    val domain: String?,
    val message: String?,
    val reason: String?
)