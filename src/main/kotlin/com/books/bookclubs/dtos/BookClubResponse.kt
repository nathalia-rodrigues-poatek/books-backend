package com.books.bookclubs.dtos

import com.books.bookclubs.models.BookClub
import com.books.bookclubs.models.Visibility
import com.books.users.dtos.UserResponse

/**
 * Public representation of a [BookClub] with its admin and members embedded.
 */
data class BookClubResponse(
    val id: Long,
    val name: String,
    val admin: UserResponse,
    val members: List<UserResponse>,
    val image: String?,
    val description: String?,
    val visibility: Visibility
) {
    companion object {
        fun from(bookClub: BookClub) = BookClubResponse(
            id = bookClub.id,
            name = bookClub.name,
            admin = UserResponse.from(bookClub.admin),
            members = bookClub.members.map(UserResponse::from),
            image = bookClub.image,
            description = bookClub.description,
            visibility = bookClub.visibility
        )
    }
}
