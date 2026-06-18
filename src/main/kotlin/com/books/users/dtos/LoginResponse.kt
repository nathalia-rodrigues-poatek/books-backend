package com.books.users.dtos

import com.books.users.models.User

/**
 * Returned on a successful login. Exposes the [token] so the client can
 * authenticate subsequent requests.
 */
data class LoginResponse(
    val id: Long,
    val name: String,
    val email: String,
    val token: String
) {
    companion object {
        fun from(user: User) = LoginResponse(
            id = user.id,
            name = user.name,
            email = user.email,
            token = user.token
        )
    }
}
