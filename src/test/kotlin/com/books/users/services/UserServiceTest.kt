package com.books.users.services

import com.books.users.dtos.CreateUserRequest
import com.books.users.dtos.LoginRequest
import com.books.users.dtos.UpdateUserRequest
import com.books.users.models.User
import com.books.users.repositories.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.Optional

class UserServiceTest {

    private val userRepository: UserRepository = mock()
    private val passwordEncoder: PasswordEncoder = mock()
    private val service = UserService(userRepository, passwordEncoder)

    private fun user(
        id: Long = 1,
        name: String = "Nathalia",
        email: String = "nathalia@poatek.com",
        token: String = "tok-123",
        password: String = "hashed",
        deleted: Boolean = false,
        blocked: Boolean = false,
        deletedDate: LocalDateTime? = null,
        blockedDate: LocalDateTime? = null
    ) = User(id, name, email, token, password, deleted, blocked, deletedDate, blockedDate)

    /** Stubs save() to echo back the entity it received. */
    private fun stubSaveEchoesArgument() {
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }
    }

    // ---------- findAll / findById ----------

    @Test
    fun `findAll returns only non-deleted users mapped to responses`() {
        whenever(userRepository.findByDeletedFalse()).thenReturn(listOf(user(id = 1), user(id = 2, email = "b@poatek.com")))

        val result = service.findAll()

        assertEquals(2, result.size)
        verify(userRepository).findByDeletedFalse()
    }

    @Test
    fun `findById returns the user when present and not deleted`() {
        whenever(userRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(user(id = 1)))

        val result = service.findById(1)

        assertEquals(1, result.id)
        assertEquals("nathalia@poatek.com", result.email)
    }

    @Test
    fun `findById throws UserNotFoundException when absent`() {
        whenever(userRepository.findByIdAndDeletedFalse(99)).thenReturn(Optional.empty())

        assertThrows(UserNotFoundException::class.java) { service.findById(99) }
    }

    // ---------- create ----------

    @Test
    fun `create hashes the password before saving and never returns it`() {
        whenever(passwordEncoder.encode("senha123")).thenReturn("hashed-value")
        stubSaveEchoesArgument()

        val response = service.create(CreateUserRequest("Nathalia", "nathalia@poatek.com", "tok-123", "senha123"))

        val captor = argumentCaptor<User>()
        verify(userRepository).save(captor.capture())
        assertEquals("hashed-value", captor.firstValue.password)
        assertEquals("nathalia@poatek.com", response.email)
    }

    // ---------- update ----------

    @Test
    fun `update changes name and email and keeps the password when none is provided`() {
        whenever(userRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(user(id = 1, password = "old-hash")))
        stubSaveEchoesArgument()

        service.update(1, UpdateUserRequest("New Name", "new@poatek.com", null))

        val captor = argumentCaptor<User>()
        verify(userRepository).save(captor.capture())
        assertEquals("New Name", captor.firstValue.name)
        assertEquals("new@poatek.com", captor.firstValue.email)
        assertEquals("old-hash", captor.firstValue.password)
        verify(passwordEncoder, never()).encode(any())
    }

    @Test
    fun `update re-hashes the password when a new one is provided`() {
        whenever(userRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(user(id = 1, password = "old-hash")))
        whenever(passwordEncoder.encode("novasenha")).thenReturn("new-hash")
        stubSaveEchoesArgument()

        service.update(1, UpdateUserRequest("Nathalia", "nathalia@poatek.com", "novasenha"))

        val captor = argumentCaptor<User>()
        verify(userRepository).save(captor.capture())
        assertEquals("new-hash", captor.firstValue.password)
    }

    @Test
    fun `update throws when the user does not exist`() {
        whenever(userRepository.findByIdAndDeletedFalse(99)).thenReturn(Optional.empty())

        assertThrows(UserNotFoundException::class.java) {
            service.update(99, UpdateUserRequest("X", "x@poatek.com", null))
        }
        verify(userRepository, never()).save(any())
    }

    // ---------- delete ----------

    @Test
    fun `delete marks the user as deleted with a timestamp`() {
        whenever(userRepository.findById(1)).thenReturn(Optional.of(user(id = 1)))
        stubSaveEchoesArgument()

        service.delete(1)

        val captor = argumentCaptor<User>()
        verify(userRepository).save(captor.capture())
        assertTrue(captor.firstValue.deleted)
        assertEquals(true, captor.firstValue.deletedDate != null)
    }

    @Test
    fun `delete is idempotent and does not save an already-deleted user`() {
        whenever(userRepository.findById(1)).thenReturn(Optional.of(user(id = 1, deleted = true, deletedDate = LocalDateTime.now())))

        service.delete(1)

        verify(userRepository, never()).save(any())
    }

    @Test
    fun `delete throws when the user does not exist`() {
        whenever(userRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(UserNotFoundException::class.java) { service.delete(99) }
    }

    // ---------- restore ----------

    @Test
    fun `restore clears the deleted flag and date`() {
        whenever(userRepository.findById(1)).thenReturn(Optional.of(user(id = 1, deleted = true, deletedDate = LocalDateTime.now())))
        stubSaveEchoesArgument()

        val response = service.restore(1)

        val captor = argumentCaptor<User>()
        verify(userRepository).save(captor.capture())
        assertFalse(captor.firstValue.deleted)
        assertNull(captor.firstValue.deletedDate)
        assertFalse(response.deleted)
    }

    @Test
    fun `restore is idempotent for a non-deleted user`() {
        whenever(userRepository.findById(1)).thenReturn(Optional.of(user(id = 1, deleted = false)))

        service.restore(1)

        verify(userRepository, never()).save(any())
    }

    @Test
    fun `restore throws when the user does not exist`() {
        whenever(userRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(UserNotFoundException::class.java) { service.restore(99) }
        verify(userRepository, never()).save(any())
    }

    // ---------- block / unblock ----------

    @Test
    fun `block sets blocked true with a timestamp`() {
        whenever(userRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(user(id = 1)))
        stubSaveEchoesArgument()

        service.block(1)

        val captor = argumentCaptor<User>()
        verify(userRepository).save(captor.capture())
        assertTrue(captor.firstValue.blocked)
        assertEquals(true, captor.firstValue.blockedDate != null)
    }

    @Test
    fun `block is idempotent for an already-blocked user`() {
        whenever(userRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(user(id = 1, blocked = true, blockedDate = LocalDateTime.now())))

        service.block(1)

        verify(userRepository, never()).save(any())
    }

    @Test
    fun `block and unblock ignore soft-deleted users and throw`() {
        // block/unblock use findByIdAndDeletedFalse, so a soft-deleted user is treated as absent.
        whenever(userRepository.findByIdAndDeletedFalse(99)).thenReturn(Optional.empty())

        assertThrows(UserNotFoundException::class.java) { service.block(99) }
        assertThrows(UserNotFoundException::class.java) { service.unblock(99) }
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `unblock clears blocked flag and date`() {
        whenever(userRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(user(id = 1, blocked = true, blockedDate = LocalDateTime.now())))
        stubSaveEchoesArgument()

        service.unblock(1)

        val captor = argumentCaptor<User>()
        verify(userRepository).save(captor.capture())
        assertFalse(captor.firstValue.blocked)
        assertNull(captor.firstValue.blockedDate)
    }

    @Test
    fun `unblock is idempotent for a non-blocked user`() {
        whenever(userRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(user(id = 1, blocked = false)))

        service.unblock(1)

        verify(userRepository, never()).save(any())
    }

    // ---------- login ----------

    @Test
    fun `login succeeds and returns the token when the password matches`() {
        whenever(userRepository.findByEmailAndDeletedFalse("nathalia@poatek.com")).thenReturn(Optional.of(user(token = "tok-abc", password = "hash")))
        whenever(passwordEncoder.matches("senha123", "hash")).thenReturn(true)

        val response = service.login(LoginRequest("nathalia@poatek.com", "senha123"))

        assertEquals("tok-abc", response.token)
    }

    @Test
    fun `login throws InvalidCredentials when the email is unknown`() {
        whenever(userRepository.findByEmailAndDeletedFalse("ghost@poatek.com")).thenReturn(Optional.empty())

        assertThrows(InvalidCredentialsException::class.java) {
            service.login(LoginRequest("ghost@poatek.com", "x"))
        }
    }

    @Test
    fun `login throws InvalidCredentials when the password does not match`() {
        whenever(userRepository.findByEmailAndDeletedFalse("nathalia@poatek.com")).thenReturn(Optional.of(user(password = "hash")))
        whenever(passwordEncoder.matches("errada", "hash")).thenReturn(false)

        assertThrows(InvalidCredentialsException::class.java) {
            service.login(LoginRequest("nathalia@poatek.com", "errada"))
        }
    }

    @Test
    fun `login throws UserBlocked when credentials are valid but the user is blocked`() {
        whenever(userRepository.findByEmailAndDeletedFalse("nathalia@poatek.com")).thenReturn(Optional.of(user(password = "hash", blocked = true)))
        whenever(passwordEncoder.matches("senha123", "hash")).thenReturn(true)

        assertThrows(UserBlockedException::class.java) {
            service.login(LoginRequest("nathalia@poatek.com", "senha123"))
        }
    }
}
