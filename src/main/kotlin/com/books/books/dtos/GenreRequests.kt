package com.books.books.dtos

import jakarta.validation.constraints.NotBlank

/**
 * Payload for creating a genre.
 */
data class CreateGenreRequest(
    @field:NotBlank(message = "name is required")
    val name: String
)

/**
 * Payload for updating an existing genre.
 */
data class UpdateGenreRequest(
    @field:NotBlank(message = "name is required")
    val name: String
)
