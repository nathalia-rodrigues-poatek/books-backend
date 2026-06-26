package com.books.bookclubs.services

import com.books.bookclubs.dtos.BookClubRequest
import com.books.bookclubs.models.BookClub
import com.books.bookclubs.models.Visibility
import com.books.bookclubs.repositories.BookClubRepository
import com.books.users.models.User
import com.books.users.repositories.UserRepository
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

class BookClubServiceTest {

    private val bookClubRepository: BookClubRepository = mock()
    private val userRepository: UserRepository = mock()
    private val service = BookClubService(bookClubRepository, userRepository)

    private fun user(id: Long, name: String = "User $id") =
        User(id = id, name = name, email = "user$id@poatek.com", token = "tok-$id", password = "hashed")

    private fun club(
        id: Long = 1,
        admin: User = user(1, "Admin"),
        members: List<User> = listOf(user(1, "Admin"))
    ) = BookClub(
        id = id,
        name = "Sci-Fi Club",
        admin = admin,
        members = members,
        image = "https://example.com/x.png",
        description = "Classics",
        visibility = Visibility.PUBLIC
    )

    private fun request(
        name: String = "Sci-Fi Club",
        adminId: Long? = 1,
        memberIds: List<Long> = listOf(1, 2),
        image: String? = "https://example.com/x.png",
        description: String? = "Classics",
        visibility: Visibility? = Visibility.PUBLIC
    ) = BookClubRequest(name, adminId, memberIds, image, description, visibility)

    /** Stubs save() to echo back the entity it received. */
    private fun stubSaveEchoesArgument() {
        whenever(bookClubRepository.save(any())).thenAnswer { it.arguments[0] as BookClub }
    }

    private fun stubUserExists(id: Long) {
        whenever(userRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(user(id)))
    }

    // ---------- findAll / findById ----------

    @Test
    fun `findAll maps book clubs to responses`() {
        whenever(bookClubRepository.findAll()).thenReturn(listOf(club(1), club(2)))

        val result = service.findAll()

        assertEquals(2, result.size)
        verify(bookClubRepository).findAll()
    }

    @Test
    fun `findById returns the club with admin and members when present`() {
        whenever(bookClubRepository.findById(1)).thenReturn(
            Optional.of(club(1, admin = user(1, "Admin"), members = listOf(user(1, "Admin"), user(2, "Member"))))
        )

        val result = service.findById(1)

        assertEquals("Sci-Fi Club", result.name)
        assertEquals(1, result.admin.id)
        assertEquals(2, result.members.size)
        assertEquals(Visibility.PUBLIC, result.visibility)
    }

    @Test
    fun `findById throws BookClubNotFoundException when absent`() {
        whenever(bookClubRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(BookClubNotFoundException::class.java) { service.findById(99) }
    }

    // ---------- create ----------

    @Test
    fun `create resolves admin and members and saves the club`() {
        stubUserExists(1)
        stubUserExists(2)
        stubSaveEchoesArgument()

        val response = service.create(request(adminId = 1, memberIds = listOf(1, 2)))

        val captor = argumentCaptor<BookClub>()
        verify(bookClubRepository).save(captor.capture())
        assertEquals(1, captor.firstValue.admin.id)
        assertEquals(2, captor.firstValue.members.size)
        assertEquals(Visibility.PUBLIC, captor.firstValue.visibility)
        assertEquals("Sci-Fi Club", response.name)
    }

    @Test
    fun `create deduplicates repeated member ids`() {
        stubUserExists(2)
        stubSaveEchoesArgument()

        service.create(request(adminId = 2, memberIds = listOf(2, 2, 2)))

        val captor = argumentCaptor<BookClub>()
        verify(bookClubRepository).save(captor.capture())
        assertEquals(1, captor.firstValue.members.size)
    }

    @Test
    fun `create throws when the admin user does not exist`() {
        whenever(userRepository.findByIdAndDeletedFalse(9999)).thenReturn(Optional.empty())

        assertThrows(BookClubUserNotFoundException::class.java) {
            service.create(request(adminId = 9999, memberIds = emptyList()))
        }
        verify(bookClubRepository, never()).save(any())
    }

    @Test
    fun `create throws when a member user does not exist`() {
        stubUserExists(1)
        whenever(userRepository.findByIdAndDeletedFalse(2)).thenReturn(Optional.empty())

        assertThrows(BookClubUserNotFoundException::class.java) {
            service.create(request(adminId = 1, memberIds = listOf(2)))
        }
        verify(bookClubRepository, never()).save(any())
    }

    // ---------- update ----------

    @Test
    fun `update changes fields, keeps the id, and saves`() {
        whenever(bookClubRepository.findById(1)).thenReturn(Optional.of(club(1)))
        stubUserExists(2)
        stubSaveEchoesArgument()

        val response = service.update(
            1,
            request(name = "Renamed", adminId = 2, memberIds = listOf(2), visibility = Visibility.PRIVATE)
        )

        val captor = argumentCaptor<BookClub>()
        verify(bookClubRepository).save(captor.capture())
        assertEquals(1, captor.firstValue.id)
        assertEquals("Renamed", captor.firstValue.name)
        assertEquals(2, captor.firstValue.admin.id)
        assertEquals(Visibility.PRIVATE, captor.firstValue.visibility)
        assertEquals("Renamed", response.name)
    }

    @Test
    fun `update throws BookClubNotFoundException when the club is missing`() {
        whenever(bookClubRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(BookClubNotFoundException::class.java) { service.update(99, request()) }
        verify(bookClubRepository, never()).save(any())
    }

    @Test
    fun `update throws when the admin user does not exist`() {
        whenever(bookClubRepository.findById(1)).thenReturn(Optional.of(club(1)))
        whenever(userRepository.findByIdAndDeletedFalse(9999)).thenReturn(Optional.empty())

        assertThrows(BookClubUserNotFoundException::class.java) {
            service.update(1, request(adminId = 9999, memberIds = emptyList()))
        }
        verify(bookClubRepository, never()).save(any())
    }

    // ---------- delete ----------

    @Test
    fun `delete removes an existing club`() {
        whenever(bookClubRepository.existsById(1)).thenReturn(true)

        service.delete(1)

        verify(bookClubRepository).deleteById(1)
    }

    @Test
    fun `delete throws BookClubNotFoundException when the club is missing`() {
        whenever(bookClubRepository.existsById(99)).thenReturn(false)

        assertThrows(BookClubNotFoundException::class.java) { service.delete(99) }
        verify(bookClubRepository, never()).deleteById(any<Long>())
    }
}
