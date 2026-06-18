package com.books.books.dtos

import jakarta.validation.constraints.NotBlank

/**
 * Payload for creating a publisher. [website] is optional.
 */
data class CreatePublisherRequest(
    @field:NotBlank(message = "name is required")
    val name: String,

    val website: String? = null
)

/**
 * Payload for updating an existing publisher. [website] is optional.
 */
data class UpdatePublisherRequest(
    @field:NotBlank(message = "name is required")
    val name: String,

    val website: String? = null
)
