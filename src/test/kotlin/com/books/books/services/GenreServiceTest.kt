package com.books.books.services

import com.books.books.dtos.GenreRequest
import com.books.books.models.Genre
import com.books.books.repositories.GenreRepository
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

class GenreServiceTest {

    private val genreRepository: GenreRepository = mock()
    private val service = GenreService(genreRepository)

    @Test
    fun `findAll maps genres to responses`() {
        whenever(genreRepository.findAll()).thenReturn(listOf(Genre(1, "Fantasy"), Genre(2, "Sci-Fi")))

        assertEquals(2, service.findAll().size)
    }

    @Test
    fun `findById returns the genre when present`() {
        whenever(genreRepository.findById(1)).thenReturn(Optional.of(Genre(1, "Fantasy")))

        assertEquals("Fantasy", service.findById(1).name)
    }

    @Test
    fun `findById throws GenreNotFoundException when absent`() {
        whenever(genreRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(GenreNotFoundException::class.java) { service.findById(99) }
    }

    @Test
    fun `create saves and returns the genre`() {
        whenever(genreRepository.save(any())).thenAnswer { it.arguments[0] as Genre }

        val response = service.create(GenreRequest("Horror"))

        val captor = argumentCaptor<Genre>()
        verify(genreRepository).save(captor.capture())
        assertEquals("Horror", captor.firstValue.name)
        assertEquals("Horror", response.name)
    }

    @Test
    fun `update changes the name`() {
        whenever(genreRepository.findById(1)).thenReturn(Optional.of(Genre(1, "Fantasy")))
        whenever(genreRepository.save(any())).thenAnswer { it.arguments[0] as Genre }

        val response = service.update(1, GenreRequest("High Fantasy"))

        assertEquals("High Fantasy", response.name)
    }

    @Test
    fun `update throws when the genre is missing`() {
        whenever(genreRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(GenreNotFoundException::class.java) { service.update(99, GenreRequest("X")) }
        verify(genreRepository, never()).save(any())
    }

    @Test
    fun `delete removes an existing genre`() {
        whenever(genreRepository.existsById(1)).thenReturn(true)

        service.delete(1)

        verify(genreRepository).deleteById(1)
    }

    @Test
    fun `delete throws when the genre is missing`() {
        whenever(genreRepository.existsById(99)).thenReturn(false)

        assertThrows(GenreNotFoundException::class.java) { service.delete(99) }
        verify(genreRepository, never()).deleteById(any<Long>())
    }
}
