package com.books.readings.services

import com.books.bookclubs.models.BookClub
import com.books.bookclubs.repositories.BookClubRepository
import com.books.bookclubs.services.BookClubNotFoundException
import com.books.books.models.Book
import com.books.books.repositories.BookRepository
import com.books.books.services.BookNotFoundException
import com.books.readings.dtos.BookClubReadingRequest
import com.books.readings.dtos.BookClubReadingResponse
import com.books.readings.models.BookClubReading
import com.books.readings.repositories.BookClubReadingRepository
import org.springframework.stereotype.Service

@Service
class BookClubReadingService(
    private val readingRepository: BookClubReadingRepository,
    private val bookClubRepository: BookClubRepository,
    private val bookRepository: BookRepository
) {

    fun findAll(): List<BookClubReadingResponse> =
        readingRepository.findAll().map(BookClubReadingResponse::from)

    fun findById(id: Long): BookClubReadingResponse =
        readingRepository.findById(id)
            .map(BookClubReadingResponse::from)
            .orElseThrow { BookClubReadingNotFoundException(id) }

    fun create(request: BookClubReadingRequest): BookClubReadingResponse {
        val bookClub = resolveBookClub(request.bookClubId!!)
        val book = resolveBook(request.bookId!!)

        if (readingRepository.existsByBookClubIdAndStartDateAndEndDate(bookClub.id, request.startDate, request.endDate)) {
            throw ReadingDateRangeConflictException()
        }

        val reading = BookClubReading(
            bookClub = bookClub,
            book = book,
            startDate = request.startDate,
            endDate = request.endDate,
            meetLink = request.meetLink,
            address = request.address
        )
        return BookClubReadingResponse.from(readingRepository.save(reading))
    }

    fun update(id: Long, request: BookClubReadingRequest): BookClubReadingResponse {
        val reading = readingRepository.findById(id).orElseThrow { BookClubReadingNotFoundException(id) }
        val bookClub = resolveBookClub(request.bookClubId!!)
        val book = resolveBook(request.bookId!!)

        if (readingRepository.existsByBookClubIdAndStartDateAndEndDateAndIdNot(bookClub.id, request.startDate, request.endDate, id)) {
            throw ReadingDateRangeConflictException()
        }

        val updated = reading.copy(
            bookClub = bookClub,
            book = book,
            startDate = request.startDate,
            endDate = request.endDate,
            meetLink = request.meetLink,
            address = request.address
        )
        return BookClubReadingResponse.from(readingRepository.save(updated))
    }

    fun delete(id: Long) {
        // TODO: turn this into a soft delete (flag the row as deleted instead of
        //  removing it), like the users domain does.
        if (!readingRepository.existsById(id)) {
            throw BookClubReadingNotFoundException(id)
        }
        readingRepository.deleteById(id)
    }

    private fun resolveBookClub(id: Long): BookClub =
        bookClubRepository.findById(id).orElseThrow { BookClubNotFoundException(id) }

    private fun resolveBook(id: Long): Book =
        bookRepository.findById(id).orElseThrow { BookNotFoundException(id) }
}

class BookClubReadingNotFoundException(id: Long) : RuntimeException("Book club reading with id $id not found")

class ReadingDateRangeConflictException :
    RuntimeException("you'r book club is already reading a book for these dates")
