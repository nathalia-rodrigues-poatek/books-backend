package com.books.books.controllers

import com.books.books.dtos.AuthorRequest
import com.books.books.dtos.AuthorResponse
import com.books.books.services.AuthorNotFoundException
import com.books.books.services.AuthorService
import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/authors")
class AuthorController(
    private val authorService: AuthorService
) {

    @GetMapping
    fun getAllAuthors(): ResponseEntity<List<AuthorResponse>> =
        ResponseEntity.ok(authorService.findAll())

    @GetMapping("/{id}")
    fun getAuthorById(@PathVariable id: Long): ResponseEntity<AuthorResponse> =
        ResponseEntity.ok(authorService.findById(id))

    @PostMapping
    fun createAuthor(@Valid @RequestBody request: AuthorRequest): ResponseEntity<AuthorResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(authorService.create(request))

    @PutMapping("/{id}")
    fun updateAuthor(
        @PathVariable id: Long,
        @Valid @RequestBody request: AuthorRequest
    ): ResponseEntity<AuthorResponse> =
        ResponseEntity.ok(authorService.update(id, request))

    @DeleteMapping("/{id}")
    fun deleteAuthor(@PathVariable id: Long): ResponseEntity<Void> {
        authorService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(AuthorNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleAuthorNotFound(ex: AuthorNotFoundException): Map<String, String?> =
        mapOf("error" to ex.message)

    @ExceptionHandler(DataIntegrityViolationException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): Map<String, String?> =
        mapOf("error" to "Author is still referenced by one or more books")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): Map<String, Any> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        return mapOf("errors" to errors)
    }
}
