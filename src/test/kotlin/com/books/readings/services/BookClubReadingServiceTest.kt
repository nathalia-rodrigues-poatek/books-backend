package com.books.readings.services

import com.books.bookclubs.models.BookClub
import com.books.bookclubs.models.Visibility
import com.books.bookclubs.repositories.BookClubRepository
import com.books.bookclubs.services.BookClubNotFoundException
import com.books.books.models.Author
import com.books.books.models.Book
import com.books.books.models.Genre
import com.books.books.models.Publisher
import com.books.books.repositories.BookRepository
import com.books.books.services.BookNotFoundException
import com.books.readings.dtos.BookClubReadingRequest
import com.books.readings.models.BookClubReading
import com.books.readings.repositories.BookClubReadingRepository
import com.books.users.models.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.Optional

class BookClubReadingServiceTest {

    private val readingRepository: BookClubReadingRepository = mock()
    private val bookClubRepository: BookClubRepository = mock()
    private val bookRepository: BookRepository = mock()
    private val service = BookClubReadingService(readingRepository, bookClubRepository, bookRepository)

    private val admin = User(id = 1, name = "Admin", email = "admin@poatek.com", token = "tok-1", password = "hashed")
    private val bookClub = BookClub(id = 1, name = "Sci-Fi Club", admin = admin, members = listOf(admin), visibility = Visibility.PUBLIC)
    private val book = Book(
        id = 1,
        name = "Dune",
        genre = Genre(1, "Sci-Fi"),
        author = Author(1, "Frank Herbert"),
        publisher = Publisher(1, "Chilton", null),
        publishedDate = LocalDate.of(1965, 8, 1),
        pageCount = 412,
        language = "en"
    )

    private val start: LocalDate = LocalDate.of(2026, 7, 1)
    private val end: LocalDate = LocalDate.of(2026, 7, 31)

    private fun reading(id: Long = 1) = BookClubReading(
        id = id, bookClub = bookClub, book = book, startDate = start, endDate = end,
        meetLink = "https://meet/x", address = "Library"
    )

    private fun request(
        bookClubId: Long? = 1,
        bookId: Long? = 1,
        startDate: LocalDate = start,
        endDate: LocalDate = end,
        meetLink: String? = "https://meet/x",
        address: String? = "Library"
    ) = BookClubReadingRequest(bookClubId, bookId, startDate, endDate, meetLink, address)

    private fun stubSaveEchoesArgument() {
        whenever(readingRepository.save(any())).thenAnswer { it.arguments[0] as BookClubReading }
    }

    private fun stubReferencesExist() {
        whenever(bookClubRepository.findById(1)).thenReturn(Optional.of(bookClub))
        whenever(bookRepository.findById(1)).thenReturn(Optional.of(book))
    }

    // ---------- findAll / findById ----------

    @Test
    fun `findAll maps readings to responses`() {
        whenever(readingRepository.findAll()).thenReturn(listOf(reading(1), reading(2)))

        val result = service.findAll()

        assertEquals(2, result.size)
    }

    @Test
    fun `findById returns the reading with its book club and book when present`() {
        whenever(readingRepository.findById(1)).thenReturn(Optional.of(reading(1)))

        val result = service.findById(1)

        assertEquals("Sci-Fi Club", result.bookClub.name)
        assertEquals("Dune", result.book.name)
        assertEquals("Library", result.address)
    }

    @Test
    fun `findById throws BookClubReadingNotFoundException when absent`() {
        whenever(readingRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(BookClubReadingNotFoundException::class.java) { service.findById(99) }
    }

    // ---------- create ----------

    @Test
    fun `create resolves references and saves the reading`() {
        stubReferencesExist()
        whenever(readingRepository.existsByBookClubIdAndStartDateAndEndDate(1, start, end)).thenReturn(false)
        stubSaveEchoesArgument()

        val response = service.create(request())

        val captor = argumentCaptor<BookClubReading>()
        verify(readingRepository).save(captor.capture())
        assertEquals(1, captor.firstValue.bookClub.id)
        assertEquals(1, captor.firstValue.book.id)
        assertEquals(start, captor.firstValue.startDate)
        assertEquals("Library", captor.firstValue.address)
        assertEquals("Dune", response.book.name)
    }

    @Test
    fun `create throws ReadingDateRangeConflictException when the club already reads in that range`() {
        stubReferencesExist()
        whenever(readingRepository.existsByBookClubIdAndStartDateAndEndDate(1, start, end)).thenReturn(true)

        val ex = assertThrows(ReadingDateRangeConflictException::class.java) { service.create(request()) }
        assertEquals("you'r book club is already reading a book for these dates", ex.message)
        verify(readingRepository, never()).save(any())
    }

    @Test
    fun `create throws when the book club does not exist`() {
        whenever(bookClubRepository.findById(9999)).thenReturn(Optional.empty())

        assertThrows(BookClubNotFoundException::class.java) { service.create(request(bookClubId = 9999)) }
        verify(readingRepository, never()).save(any())
    }

    @Test
    fun `create throws when the book does not exist`() {
        whenever(bookClubRepository.findById(1)).thenReturn(Optional.of(bookClub))
        whenever(bookRepository.findById(9999)).thenReturn(Optional.empty())

        assertThrows(BookNotFoundException::class.java) { service.create(request(bookId = 9999)) }
        verify(readingRepository, never()).save(any())
    }

    // ---------- update ----------

    @Test
    fun `update changes fields, keeps the id, and saves`() {
        whenever(readingRepository.findById(1)).thenReturn(Optional.of(reading(1)))
        stubReferencesExist()
        whenever(readingRepository.existsByBookClubIdAndStartDateAndEndDateAndIdNot(eq(1), any(), any(), eq(1))).thenReturn(false)
        stubSaveEchoesArgument()

        val newEnd = LocalDate.of(2026, 7, 15)
        val response = service.update(1, request(endDate = newEnd, address = "Online only"))

        val captor = argumentCaptor<BookClubReading>()
        verify(readingRepository).save(captor.capture())
        assertEquals(1, captor.firstValue.id)
        assertEquals(newEnd, captor.firstValue.endDate)
        assertEquals("Online only", captor.firstValue.address)
        assertEquals(newEnd, response.endDate)
    }

    @Test
    fun `update throws ReadingDateRangeConflictException when another reading uses that range`() {
        whenever(readingRepository.findById(1)).thenReturn(Optional.of(reading(1)))
        stubReferencesExist()
        whenever(readingRepository.existsByBookClubIdAndStartDateAndEndDateAndIdNot(eq(1), any(), any(), eq(1))).thenReturn(true)

        assertThrows(ReadingDateRangeConflictException::class.java) { service.update(1, request()) }
        verify(readingRepository, never()).save(any())
    }

    @Test
    fun `update throws BookClubReadingNotFoundException when the reading is missing`() {
        whenever(readingRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(BookClubReadingNotFoundException::class.java) { service.update(99, request()) }
        verify(readingRepository, never()).save(any())
    }

    // ---------- delete ----------

    @Test
    fun `delete removes an existing reading`() {
        whenever(readingRepository.existsById(1)).thenReturn(true)

        service.delete(1)

        verify(readingRepository).deleteById(1)
    }

    @Test
    fun `delete throws BookClubReadingNotFoundException when the reading is missing`() {
        whenever(readingRepository.existsById(99)).thenReturn(false)

        assertThrows(BookClubReadingNotFoundException::class.java) { service.delete(99) }
        verify(readingRepository, never()).deleteById(any<Long>())
    }
}
