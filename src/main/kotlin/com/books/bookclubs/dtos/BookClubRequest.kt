package com.books.bookclubs.dtos

import com.books.bookclubs.models.Visibility
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * Payload for creating or fully replacing a book club. [adminId] and every id in
 * [memberIds] must reference existing (non-deleted) users.
 */
data class BookClubRequest(
    @field:NotBlank(message = "name is required")
    val name: String,

    @field:NotNull(message = "adminId is required")
    val adminId: Long?,

    val memberIds: List<Long> = emptyList(),

    val image: String? = null,

    val description: String? = null,

    @field:NotNull(message = "visibility is required")
    val visibility: Visibility?
)
