package me.squeezymo.core.domain.data

typealias ID = String
typealias StreamingServiceID = String
typealias PlaylistTitle = String

data class EntityWithExternalIDs<E>(
    val entity: E,
    val ids: Map<StreamingServiceID, ID>
) {

    inline fun <E2> map(mapper: E.() -> E2): EntityWithExternalIDs<E2> {
        return EntityWithExternalIDs(
            entity = mapper(entity),
            ids = ids
        )
    }

}

data class BaseTrack(
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMillis: Long?,
    val audioPreviewUrl: String?,
    val thumbnailUrl: String?
)

data class BasePlaylist(
    val id: ID, // TODO Inconsistency. BaseTrack's ID is taken from EntityWithExternalIDs
    val title: PlaylistTitle,
    val tracks: List<EntityWithExternalIDs<BaseTrack>>,
    val thumbnailUrl: String?
)
