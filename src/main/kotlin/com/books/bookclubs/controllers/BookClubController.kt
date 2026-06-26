package com.books.bookclubs.controllers

import com.books.bookclubs.dtos.BookClubRequest
import com.books.bookclubs.dtos.BookClubResponse
import com.books.bookclubs.services.BookClubNotFoundException
import com.books.bookclubs.services.BookClubService
import com.books.bookclubs.services.BookClubUserNotFoundException
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
@RequestMapping("/api/book-clubs")
class BookClubController(
    private val bookClubService: BookClubService
) {

    @GetMapping
    fun getAllBookClubs(): ResponseEntity<List<BookClubResponse>> =
        ResponseEntity.ok(bookClubService.findAll())

    @GetMapping("/{id}")
    fun getBookClubById(@PathVariable id: Long): ResponseEntity<BookClubResponse> =
        ResponseEntity.ok(bookClubService.findById(id))

    @PostMapping
    fun createBookClub(@Valid @RequestBody request: BookClubRequest): ResponseEntity<BookClubResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(bookClubService.create(request))

    @PutMapping("/{id}")
    fun updateBookClub(
        @PathVariable id: Long,
        @Valid @RequestBody request: BookClubRequest
    ): ResponseEntity<BookClubResponse> =
        ResponseEntity.ok(bookClubService.update(id, request))

    @DeleteMapping("/{id}")
    fun deleteBookClub(@PathVariable id: Long): ResponseEntity<Void> {
        bookClubService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(BookClubNotFoundException::class, BookClubUserNotFoundException::class)
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
