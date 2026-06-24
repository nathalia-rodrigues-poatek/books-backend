package com.books.books.dtos

import com.books.books.models.Publisher

/**
 * Public representation of a [Publisher].
 */
data class PublisherResponse(
    val id: Long,
    val name: String,
    val website: String?
) {
    companion object {
        fun from(publisher: Publisher) =
            PublisherResponse(id = publisher.id, name = publisher.name, website = publisher.website)
    }
}
