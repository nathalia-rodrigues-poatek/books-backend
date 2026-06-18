package com.books.users.dtos

import com.books.users.models.User
import java.time.LocalDateTime

/**
 * Public representation of a [User]. The sensitive `token` field is intentionally omitted.
 */
data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val deleted: Boolean,
    val blocked: Boolean,
    val deletedDate: LocalDateTime?,
    val blockedDate: LocalDateTime?
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id,
            name = user.name,
            email = user.email,
            deleted = user.deleted,
            blocked = user.blocked,
            deletedDate = user.deletedDate,
            blockedDate = user.blockedDate
        )
    }
}