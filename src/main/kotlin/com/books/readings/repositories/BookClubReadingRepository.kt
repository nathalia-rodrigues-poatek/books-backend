package com.books.readings.repositories

import com.books.readings.models.BookClubReading
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface BookClubReadingRepository : JpaRepository<BookClubReading, Long> {

    /** True when the club already has a reading scheduled for exactly this date range. */
    fun existsByBookClubIdAndStartDateAndEndDate(
        bookClubId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Boolean

    /** Same check, excluding a given reading id (used on update). */
    fun existsByBookClubIdAndStartDateAndEndDateAndIdNot(
        bookClubId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        id: Long
    ): Boolean
}
