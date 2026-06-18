package com.books.users.services

import com.books.users.dtos.CreateUserRequest
import com.books.users.dtos.LoginRequest
import com.books.users.dtos.LoginResponse
import com.books.users.dtos.UpdateUserRequest
import com.books.users.dtos.UserResponse
import com.books.users.repositories.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun findAll(): List<UserResponse> =
        userRepository.findByDeletedFalse().map(UserResponse::from)

    fun findById(id: Long): UserResponse =
        userRepository.findByIdAndDeletedFalse(id)
            .map(UserResponse::from)
            .orElseThrow { UserNotFoundException(id) }

    fun create(request: CreateUserRequest): UserResponse {
        val entity = request.toEntity().copy(password = passwordEncoder.encode(request.password))
        return UserResponse.from(userRepository.save(entity))
    }

    fun update(id: Long, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findByIdAndDeletedFalse(id).orElseThrow { UserNotFoundException(id) }
        val updated = user.copy(
            name = request.name,
            email = request.email,
            password = request.password?.let { passwordEncoder.encode(it) } ?: user.password
        )
        return UserResponse.from(userRepository.save(updated))
    }

    fun delete(id: Long) {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
        if (user.deleted) {
            return
        }
        userRepository.save(user.copy(deleted = true, deletedDate = LocalDateTime.now()))
    }

    fun restore(id: Long): UserResponse {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
        if (!user.deleted) {
            return UserResponse.from(user)
        }
        return UserResponse.from(userRepository.save(user.copy(deleted = false, deletedDate = null)))
    }

    fun block(id: Long): UserResponse {
        val user = userRepository.findByIdAndDeletedFalse(id).orElseThrow { UserNotFoundException(id) }
        if (user.blocked) {
            return UserResponse.from(user)
        }
        return UserResponse.from(userRepository.save(user.copy(blocked = true, blockedDate = LocalDateTime.now())))
    }

    fun unblock(id: Long): UserResponse {
        val user = userRepository.findByIdAndDeletedFalse(id).orElseThrow { UserNotFoundException(id) }
        if (!user.blocked) {
            return UserResponse.from(user)
        }
        return UserResponse.from(userRepository.save(user.copy(blocked = false, blockedDate = null)))
    }

    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmailAndDeletedFalse(request.email)
            .orElseThrow { InvalidCredentialsException() }
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw InvalidCredentialsException()
        }
        if (user.blocked) {
            throw UserBlockedException()
        }
        return LoginResponse.from(user)
    }
}

class UserNotFoundException(id: Long) : RuntimeException("User with id $id not found")

class InvalidCredentialsException : RuntimeException("Invalid email or password")

class UserBlockedException : RuntimeException("User is blocked")