package com.books.users.dtos

import com.books.users.models.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Payload for creating a new [User].
 */
data class CreateUserRequest(
    @field:NotBlank(message = "name is required")
    val name: String,

    @field:NotBlank(message = "email is required")
    @field:Email(message = "email must be a valid email address")
    val email: String,

    @field:NotBlank(message = "token is required")
    val token: String,

    @field:NotBlank(message = "password is required")
    @field:Size(min = 6, message = "password must be at least 6 characters")
    val password: String
) {
    fun toEntity() = User(
        name = name,
        email = email,
        token = token,
        password = password
    )
}
