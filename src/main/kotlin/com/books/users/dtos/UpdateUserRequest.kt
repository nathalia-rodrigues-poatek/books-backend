package com.books.users.dtos

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Payload for updating an existing user. [password] is optional — when null or
 * omitted the current password is kept.
 */
data class UpdateUserRequest(
    @field:NotBlank(message = "name is required")
    val name: String,

    @field:NotBlank(message = "email is required")
    @field:Email(message = "email must be a valid email address")
    val email: String,

    @field:Size(min = 6, message = "password must be at least 6 characters")
    val password: String? = null
)
