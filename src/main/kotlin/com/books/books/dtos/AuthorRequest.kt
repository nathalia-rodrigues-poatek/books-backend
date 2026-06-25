package com.books.books.dtos

import jakarta.validation.constraints.NotBlank

/**
 * Payload for creating or fully replacing an author.
 */
data class AuthorRequest(
    @field:NotBlank(message = "name is required")
    val name: String
)
