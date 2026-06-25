package com.books.books.dtos

import com.books.books.models.Book
import java.time.LocalDate

/**
 * Public representation of a [Book] with its related genre, author and
 * publisher embedded.
 */
data class BookResponse(
    val id: Long,
    val name: String,
    val genre: GenreResponse,
    val author: AuthorResponse,
    val publisher: PublisherResponse,
    val publishedDate: LocalDate?,
    val pageCount: Int?,
    val language: String?
) {
    companion object {
        fun from(book: Book) = BookResponse(
            id = book.id,
            name = book.name,
            genre = GenreResponse.from(book.genre),
            author = AuthorResponse.from(book.author),
            publisher = PublisherResponse.from(book.publisher),
            publishedDate = book.publishedDate,
            pageCount = book.pageCount,
            language = book.language
        )
    }
}
