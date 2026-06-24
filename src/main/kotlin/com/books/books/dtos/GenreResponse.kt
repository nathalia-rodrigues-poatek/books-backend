package com.books.books.dtos

import com.books.books.models.Genre

/**
 * Public representation of a [Genre].
 */
data class GenreResponse(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(genre: Genre) = GenreResponse(id = genre.id, name = genre.name)
    }
}
