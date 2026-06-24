package com.books.books.dtos

import jakarta.validation.constraints.NotBlank

/**
 * Payload for creating or fully replacing a publisher. [website] is optional.
 */
data class PublisherRequest(
    @field:NotBlank(message = "name is required")
    val name: String,

    val website: String? = null
)
