package com.books.books.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDate

/**
 * Payload for creating a book. [genreId], [authorId] and [publisherId] must
 * reference existing records.
 */
data class CreateBookRequest(
    @field:NotBlank(message = "name is required")
    val name: String,

    @field:NotNull(message = "genreId is required")
    val genreId: Long?,

    @field:NotNull(message = "authorId is required")
    val authorId: Long?,

    @field:NotNull(message = "publisherId is required")
    val publisherId: Long?,

    val publishedDate: LocalDate? = null,

    @field:Positive(message = "pageCount must be positive")
    val pageCount: Int? = null,

    val language: String? = null
)

/**
 * Payload for updating an existing book (full replacement).
 */
data class UpdateBookRequest(
    @field:NotBlank(message = "name is required")
    val name: String,

    @field:NotNull(message = "genreId is required")
    val genreId: Long?,

    @field:NotNull(message = "authorId is required")
    val authorId: Long?,

    @field:NotNull(message = "publisherId is required")
    val publisherId: Long?,

    val publishedDate: LocalDate? = null,

    @field:Positive(message = "pageCount must be positive")
    val pageCount: Int? = null,

    val language: String? = null
)
