package me.squeezymo.oauth.api.exception

class ApiException(
    val serviceId: String,
    val status: Int,
    val originalMessage: String
) : RuntimeException() {

    override val message: String
        get() = "API exception: status=${status}, message=\"${originalMessage}\", serviceId=\"$serviceId\""

}