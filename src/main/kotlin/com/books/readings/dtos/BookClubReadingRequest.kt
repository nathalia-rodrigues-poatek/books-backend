package com.books.readings.dtos

import jakarta.validation.constraints.NotNull
import java.time.LocalDate

/**
 * Payload for creating or fully replacing a book club reading. [bookClubId] and
 * [bookId] must reference existing records.
 */
data class BookClubReadingRequest(
    @field:NotNull(message = "bookClubId is required")
    val bookClubId: Long?,

    @field:NotNull(message = "bookId is required")
    val bookId: Long?,

    @field:NotNull(message = "startDate is required")
    val startDate: LocalDate?,

    @field:NotNull(message = "endDate is required")
    val endDate: LocalDate?,

    val meetLink: String? = null
)
