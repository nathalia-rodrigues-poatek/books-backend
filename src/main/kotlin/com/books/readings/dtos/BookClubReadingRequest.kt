package com.books.readings.dtos

import jakarta.validation.constraints.NotNull
import java.time.LocalDate

/**
 * Payload for creating or fully replacing a book club reading. [bookClubId] and
 * [bookId] must reference existing records. [startDate] and [endDate] are
 * required; [meetLink] and [address] are optional.
 */
data class BookClubReadingRequest(
    @field:NotNull(message = "bookClubId is required")
    val bookClubId: Long?,

    @field:NotNull(message = "bookId is required")
    val bookId: Long?,

    val startDate: LocalDate,

    val endDate: LocalDate,

    val meetLink: String? = null,

    val address: String? = null
)
