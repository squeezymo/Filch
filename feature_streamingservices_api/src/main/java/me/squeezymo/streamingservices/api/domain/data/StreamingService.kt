package me.squeezymo.streamingservices.api.domain.data

enum class StreamingService(
    val id: String,
    val isEnabled: Boolean
) {

    VK(
        id = "vk",
        isEnabled = false
    ),
    APPLE_MUSIC(
        id = "appleMusic",
        isEnabled = false
    ),
    YANDEX_MUSIC(
        id = "yandexMusic",
        isEnabled = false
    ),
    SPOTIFY(
        id = "spotify",
        isEnabled = true
    ),
    DEEZER(
        id = "deezer",
        isEnabled = true
    ),
    YOUTUBE(
        id = "youtubeMusic",
        isEnabled = true
    );

    companion object {

        fun findById(id: String): StreamingService? {
            return values().find { it.id == id }
        }

        fun requireById(id: String): StreamingService {
            return requireNotNull(findById(id)) {
                "No streaming service found by id=$id"
            }
        }

    }

}
