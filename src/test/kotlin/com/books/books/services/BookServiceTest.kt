package com.books.books.services

import com.books.books.dtos.BookRequest
import com.books.books.models.Author
import com.books.books.models.Book
import com.books.books.models.Genre
import com.books.books.models.Publisher
import com.books.books.repositories.AuthorRepository
import com.books.books.repositories.BookRepository
import com.books.books.repositories.GenreRepository
import com.books.books.repositories.PublisherRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.Optional

class BookServiceTest {

    private val bookRepository: BookRepository = mock()
    private val genreRepository: GenreRepository = mock()
    private val authorRepository: AuthorRepository = mock()
    private val publisherRepository: PublisherRepository = mock()
    private val service = BookService(bookRepository, genreRepository, authorRepository, publisherRepository)

    private val genre = Genre(id = 1, name = "Fantasy")
    private val author = Author(id = 2, name = "J.R.R. Tolkien")
    private val publisher = Publisher(id = 3, name = "Allen & Unwin", website = null)

    private fun book(id: Long = 1) = Book(
        id = id,
        name = "The Hobbit",
        genre = genre,
        author = author,
        publisher = publisher,
        publishedDate = LocalDate.of(1937, 9, 21),
        pageCount = 310,
        language = "en"
    )

    private fun request(
        name: String = "The Hobbit",
        genreId: Long? = 1,
        authorId: Long? = 2,
        publisherId: Long? = 3,
        publishedDate: LocalDate? = LocalDate.of(1937, 9, 21),
        pageCount: Int? = 310,
        language: String? = "en"
    ) = BookRequest(name, genreId, authorId, publisherId, publishedDate, pageCount, language)

    /** Stubs save() to echo back the entity it received. */
    private fun stubSaveEchoesArgument() {
        whenever(bookRepository.save(any())).thenAnswer { it.arguments[0] as Book }
    }

    private fun stubRelationshipsExist() {
        whenever(genreRepository.findById(1)).thenReturn(Optional.of(genre))
        whenever(authorRepository.findById(2)).thenReturn(Optional.of(author))
        whenever(publisherRepository.findById(3)).thenReturn(Optional.of(publisher))
    }

    // ---------- findAll / findById ----------

    @Test
    fun `findAll maps books to responses`() {
        whenever(bookRepository.findAll()).thenReturn(listOf(book(1), book(2)))

        val result = service.findAll()

        assertEquals(2, result.size)
        verify(bookRepository).findAll()
    }

    @Test
    fun `findById returns the book with its relationships when present`() {
        whenever(bookRepository.findById(1)).thenReturn(Optional.of(book(1)))

        val result = service.findById(1)

        assertEquals("The Hobbit", result.name)
        assertEquals("Fantasy", result.genre.name)
        assertEquals("J.R.R. Tolkien", result.author.name)
        assertEquals("Allen & Unwin", result.publisher.name)
    }

    @Test
    fun `findById throws BookNotFoundException when absent`() {
        whenever(bookRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(BookNotFoundException::class.java) { service.findById(99) }
    }

    // ---------- create ----------

    @Test
    fun `create resolves relationships and saves the book`() {
        stubRelationshipsExist()
        stubSaveEchoesArgument()

        val response = service.create(request())

        val captor = argumentCaptor<Book>()
        verify(bookRepository).save(captor.capture())
        assertEquals("The Hobbit", captor.firstValue.name)
        assertEquals(genre, captor.firstValue.genre)
        assertEquals(author, captor.firstValue.author)
        assertEquals(publisher, captor.firstValue.publisher)
        assertEquals(310, captor.firstValue.pageCount)
        assertEquals("Fantasy", response.genre.name)
    }

    @Test
    fun `create throws GenreNotFoundException when the genre is missing`() {
        whenever(genreRepository.findById(1)).thenReturn(Optional.empty())

        assertThrows(GenreNotFoundException::class.java) { service.create(request()) }
        verify(bookRepository, never()).save(any())
    }

    @Test
    fun `create throws AuthorNotFoundException when the author is missing`() {
        whenever(genreRepository.findById(1)).thenReturn(Optional.of(genre))
        whenever(authorRepository.findById(2)).thenReturn(Optional.empty())

        assertThrows(AuthorNotFoundException::class.java) { service.create(request()) }
        verify(bookRepository, never()).save(any())
    }

    @Test
    fun `create throws PublisherNotFoundException when the publisher is missing`() {
        whenever(genreRepository.findById(1)).thenReturn(Optional.of(genre))
        whenever(authorRepository.findById(2)).thenReturn(Optional.of(author))
        whenever(publisherRepository.findById(3)).thenReturn(Optional.empty())

        assertThrows(PublisherNotFoundException::class.java) { service.create(request()) }
        verify(bookRepository, never()).save(any())
    }

    // ---------- update ----------

    @Test
    fun `update changes fields, keeps the id, and saves`() {
        whenever(bookRepository.findById(1)).thenReturn(Optional.of(book(1)))
        stubRelationshipsExist()
        stubSaveEchoesArgument()

        val response = service.update(1, request(name = "The Hobbit (revised)", pageCount = 999))

        val captor = argumentCaptor<Book>()
        verify(bookRepository).save(captor.capture())
        assertEquals(1, captor.firstValue.id)
        assertEquals("The Hobbit (revised)", captor.firstValue.name)
        assertEquals(999, captor.firstValue.pageCount)
        assertEquals("The Hobbit (revised)", response.name)
    }

    @Test
    fun `update throws BookNotFoundException when the book is missing`() {
        whenever(bookRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(BookNotFoundException::class.java) { service.update(99, request()) }
        verify(bookRepository, never()).save(any())
    }

    @Test
    fun `update throws when a related entity is missing`() {
        whenever(bookRepository.findById(1)).thenReturn(Optional.of(book(1)))
        whenever(genreRepository.findById(1)).thenReturn(Optional.empty())

        assertThrows(GenreNotFoundException::class.java) { service.update(1, request()) }
        verify(bookRepository, never()).save(any())
    }

    // ---------- delete ----------

    @Test
    fun `delete removes an existing book`() {
        whenever(bookRepository.existsById(1)).thenReturn(true)

        service.delete(1)

        verify(bookRepository).deleteById(1)
    }

    @Test
    fun `delete throws BookNotFoundException when the book is missing`() {
        whenever(bookRepository.existsById(99)).thenReturn(false)

        assertThrows(BookNotFoundException::class.java) { service.delete(99) }
        verify(bookRepository, never()).deleteById(any<Long>())
    }
}
