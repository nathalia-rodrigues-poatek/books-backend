package com.books.books.controllers

import com.books.books.dtos.BookRequest
import com.books.books.dtos.BookResponse
import com.books.books.services.AuthorNotFoundException
import com.books.books.services.BookNotFoundException
import com.books.books.services.BookService
import com.books.books.services.GenreNotFoundException
import com.books.books.services.PublisherNotFoundException
import jakarta.validation.Valid
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
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService
) {

    @GetMapping
    fun getAllBooks(): ResponseEntity<List<BookResponse>> =
        ResponseEntity.ok(bookService.findAll())

    @GetMapping("/{id}")
    fun getBookById(@PathVariable id: Long): ResponseEntity<BookResponse> =
        ResponseEntity.ok(bookService.findById(id))

    @PostMapping
    fun createBook(@Valid @RequestBody request: BookRequest): ResponseEntity<BookResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request))

    @PutMapping("/{id}")
    fun updateBook(
        @PathVariable id: Long,
        @Valid @RequestBody request: BookRequest
    ): ResponseEntity<BookResponse> =
        ResponseEntity.ok(bookService.update(id, request))

    @DeleteMapping("/{id}")
    fun deleteBook(@PathVariable id: Long): ResponseEntity<Void> {
        bookService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(
        BookNotFoundException::class,
        GenreNotFoundException::class,
        AuthorNotFoundException::class,
        PublisherNotFoundException::class
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: RuntimeException): Map<String, String?> =
        mapOf("error" to ex.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): Map<String, Any> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        return mapOf("errors" to errors)
    }
}
