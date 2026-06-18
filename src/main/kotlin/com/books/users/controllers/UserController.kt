package com.books.users.controllers

import com.books.users.dtos.CreateUserRequest
import com.books.users.dtos.LoginRequest
import com.books.users.dtos.LoginResponse
import com.books.users.dtos.UpdateUserRequest
import com.books.users.dtos.UserResponse
import com.books.users.services.InvalidCredentialsException
import com.books.users.services.UserBlockedException
import com.books.users.services.UserNotFoundException
import com.books.users.services.UserService
import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>> =
        ResponseEntity.ok(userService.findAll())

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponse> =
        ResponseEntity.ok(userService.findById(id))

    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request))

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> =
        ResponseEntity.ok(userService.update(id, request))

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/restore")
    fun restoreUser(@PathVariable id: Long): ResponseEntity<UserResponse> =
        ResponseEntity.ok(userService.restore(id))

    @PostMapping("/{id}/block")
    fun blockUser(@PathVariable id: Long): ResponseEntity<UserResponse> =
        ResponseEntity.ok(userService.block(id))

    @PostMapping("/{id}/unblock")
    fun unblockUser(@PathVariable id: Long): ResponseEntity<UserResponse> =
        ResponseEntity.ok(userService.unblock(id))

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> =
        ResponseEntity.ok(userService.login(request))

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleUserNotFound(ex: UserNotFoundException): Map<String, String?> =
        mapOf("error" to ex.message)

    @ExceptionHandler(DataIntegrityViolationException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): Map<String, String?> =
        mapOf("error" to "User with the same email or token already exists")

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): Map<String, String?> =
        mapOf("error" to ex.message)

    @ExceptionHandler(UserBlockedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleUserBlocked(ex: UserBlockedException): Map<String, String?> =
        mapOf("error" to ex.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): Map<String, Any> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        return mapOf("errors" to errors)
    }
}