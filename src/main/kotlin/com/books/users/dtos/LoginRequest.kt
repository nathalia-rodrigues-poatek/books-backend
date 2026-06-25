package com.books.users.dtos

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * Credentials submitted to the login endpoint.
 */
data class LoginRequest(
    @field:NotBlank(message = "email is required")
    @field:Email(message = "email must be a valid email address")
    val email: String,

    @field:NotBlank(message = "password is required")
    val password: String
)
