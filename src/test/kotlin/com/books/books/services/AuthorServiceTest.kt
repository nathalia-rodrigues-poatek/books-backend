package com.books.books.services

import com.books.books.dtos.AuthorRequest
import com.books.books.models.Author
import com.books.books.repositories.AuthorRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class AuthorServiceTest {

    private val authorRepository: AuthorRepository = mock()
    private val service = AuthorService(authorRepository)

    @Test
    fun `findAll maps authors to responses`() {
        whenever(authorRepository.findAll()).thenReturn(listOf(Author(1, "Tolkien"), Author(2, "Herbert")))

        assertEquals(2, service.findAll().size)
    }

    @Test
    fun `findById returns the author when present`() {
        whenever(authorRepository.findById(1)).thenReturn(Optional.of(Author(1, "Tolkien")))

        assertEquals("Tolkien", service.findById(1).name)
    }

    @Test
    fun `findById throws AuthorNotFoundException when absent`() {
        whenever(authorRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(AuthorNotFoundException::class.java) { service.findById(99) }
    }

    @Test
    fun `create saves and returns the author`() {
        whenever(authorRepository.save(any())).thenAnswer { it.arguments[0] as Author }

        val response = service.create(AuthorRequest("Ursula K. Le Guin"))

        val captor = argumentCaptor<Author>()
        verify(authorRepository).save(captor.capture())
        assertEquals("Ursula K. Le Guin", captor.firstValue.name)
        assertEquals("Ursula K. Le Guin", response.name)
    }

    @Test
    fun `update changes the name`() {
        whenever(authorRepository.findById(1)).thenReturn(Optional.of(Author(1, "Tolkien")))
        whenever(authorRepository.save(any())).thenAnswer { it.arguments[0] as Author }

        val response = service.update(1, AuthorRequest("J.R.R. Tolkien"))

        assertEquals("J.R.R. Tolkien", response.name)
    }

    @Test
    fun `update throws when the author is missing`() {
        whenever(authorRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(AuthorNotFoundException::class.java) { service.update(99, AuthorRequest("X")) }
        verify(authorRepository, never()).save(any())
    }

    @Test
    fun `delete removes an existing author`() {
        whenever(authorRepository.existsById(1)).thenReturn(true)

        service.delete(1)

        verify(authorRepository).deleteById(1)
    }

    @Test
    fun `delete throws when the author is missing`() {
        whenever(authorRepository.existsById(99)).thenReturn(false)

        assertThrows(AuthorNotFoundException::class.java) { service.delete(99) }
        verify(authorRepository, never()).deleteById(any<Long>())
    }
}
