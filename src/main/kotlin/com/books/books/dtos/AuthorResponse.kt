package com.books.books.dtos

import com.books.books.models.Author

/**
 * Public representation of an [Author].
 */
data class AuthorResponse(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(author: Author) = AuthorResponse(id = author.id, name = author.name)
    }
}
