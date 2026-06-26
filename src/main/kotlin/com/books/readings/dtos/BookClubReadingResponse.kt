package com.books.readings.dtos

import com.books.bookclubs.dtos.BookClubResponse
import com.books.books.dtos.BookResponse
import com.books.readings.models.BookClubReading
import java.time.LocalDate

/**
 * Public representation of a [BookClubReading] with its book club and book embedded.
 */
data class BookClubReadingResponse(
    val id: Long,
    val bookClub: BookClubResponse,
    val book: BookResponse,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val meetLink: String?,
    val address: String?
) {
    companion object {
        fun from(reading: BookClubReading) = BookClubReadingResponse(
            id = reading.id,
            bookClub = BookClubResponse.from(reading.bookClub),
            book = BookResponse.from(reading.book),
            startDate = reading.startDate,
            endDate = reading.endDate,
            meetLink = reading.meetLink,
            address = reading.address
        )
    }
}
