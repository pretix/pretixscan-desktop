package eu.pretix.desktop.cache

import kotlinx.serialization.Serializable
import org.joda.time.DateTime

@Serializable
data class EventSelection(
    val eventSlug: String,
    val eventName: String,
    val subEventId: Long?,
    val checkInListId: Long,
    val checkInListName: String,
    @Serializable(with = DateTimeSerializer::class)
    val dateFrom: DateTime?,
    @Serializable(with = DateTimeSerializer::class)
    val dateTo: DateTime?,
)