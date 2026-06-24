package com.books.books.dtos

import jakarta.validation.constraints.NotBlank

/**
 * Payload for creating or fully replacing a genre.
 */
data class GenreRequest(
    @field:NotBlank(message = "name is required")
    val name: String
)
