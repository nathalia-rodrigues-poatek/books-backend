package com.books.books.services

import com.books.books.dtos.PublisherRequest
import com.books.books.models.Publisher
import com.books.books.repositories.PublisherRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class PublisherServiceTest {

    private val publisherRepository: PublisherRepository = mock()
    private val service = PublisherService(publisherRepository)

    @Test
    fun `findAll maps publishers to responses`() {
        whenever(publisherRepository.findAll()).thenReturn(
            listOf(Publisher(1, "O'Reilly", "https://oreilly.com"), Publisher(2, "Manning", null))
        )

        assertEquals(2, service.findAll().size)
    }

    @Test
    fun `findById returns the publisher when present`() {
        whenever(publisherRepository.findById(1)).thenReturn(Optional.of(Publisher(1, "O'Reilly", "https://oreilly.com")))

        val response = service.findById(1)

        assertEquals("O'Reilly", response.name)
        assertEquals("https://oreilly.com", response.website)
    }

    @Test
    fun `findById throws PublisherNotFoundException when absent`() {
        whenever(publisherRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(PublisherNotFoundException::class.java) { service.findById(99) }
    }

    @Test
    fun `create saves the name and website`() {
        whenever(publisherRepository.save(any())).thenAnswer { it.arguments[0] as Publisher }

        val response = service.create(PublisherRequest("O'Reilly", "https://oreilly.com"))

        val captor = argumentCaptor<Publisher>()
        verify(publisherRepository).save(captor.capture())
        assertEquals("O'Reilly", captor.firstValue.name)
        assertEquals("https://oreilly.com", captor.firstValue.website)
        assertEquals("https://oreilly.com", response.website)
    }

    @Test
    fun `create allows a null website`() {
        whenever(publisherRepository.save(any())).thenAnswer { it.arguments[0] as Publisher }

        val response = service.create(PublisherRequest("Indie Press", null))

        assertNull(response.website)
    }

    @Test
    fun `update changes name and website`() {
        whenever(publisherRepository.findById(1)).thenReturn(Optional.of(Publisher(1, "Old Name", "http://old")))
        whenever(publisherRepository.save(any())).thenAnswer { it.arguments[0] as Publisher }

        val response = service.update(1, PublisherRequest("New Name", "http://new"))

        assertEquals("New Name", response.name)
        assertEquals("http://new", response.website)
    }

    @Test
    fun `update throws when the publisher is missing`() {
        whenever(publisherRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(PublisherNotFoundException::class.java) { service.update(99, PublisherRequest("X", null)) }
        verify(publisherRepository, never()).save(any())
    }

    @Test
    fun `delete removes an existing publisher`() {
        whenever(publisherRepository.existsById(1)).thenReturn(true)

        service.delete(1)

        verify(publisherRepository).deleteById(1)
    }

    @Test
    fun `delete throws when the publisher is missing`() {
        whenever(publisherRepository.existsById(99)).thenReturn(false)

        assertThrows(PublisherNotFoundException::class.java) { service.delete(99) }
        verify(publisherRepository, never()).deleteById(any<Long>())
    }
}
