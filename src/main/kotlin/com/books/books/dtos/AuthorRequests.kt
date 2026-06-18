package com.books.books.dtos

import jakarta.validation.constraints.NotBlank

/**
 * Payload for creating an author.
 */
data class CreateAuthorRequest(
    @field:NotBlank(message = "name is required")
    val name: String
)

/**
 * Payload for updating an existing author.
 */
data class UpdateAuthorRequest(
    @field:NotBlank(message = "name is required")
    val name: String
)
