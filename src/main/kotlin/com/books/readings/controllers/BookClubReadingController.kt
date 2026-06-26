package com.books.readings.controllers

import com.books.bookclubs.services.BookClubNotFoundException
import com.books.books.services.BookNotFoundException
import com.books.readings.dtos.BookClubReadingRequest
import com.books.readings.dtos.BookClubReadingResponse
import com.books.readings.services.BookClubReadingNotFoundException
import com.books.readings.services.BookClubReadingService
import com.books.readings.services.ReadingDateRangeConflictException
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
@RequestMapping("/api/book-club-readings")
class BookClubReadingController(
    private val readingService: BookClubReadingService
) {

    @GetMapping
    fun getAllReadings(): ResponseEntity<List<BookClubReadingResponse>> =
        ResponseEntity.ok(readingService.findAll())

    @GetMapping("/{id}")
    fun getReadingById(@PathVariable id: Long): ResponseEntity<BookClubReadingResponse> =
        ResponseEntity.ok(readingService.findById(id))

    @PostMapping
    fun createReading(@Valid @RequestBody request: BookClubReadingRequest): ResponseEntity<BookClubReadingResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(readingService.create(request))

    @PutMapping("/{id}")
    fun updateReading(
        @PathVariable id: Long,
        @Valid @RequestBody request: BookClubReadingRequest
    ): ResponseEntity<BookClubReadingResponse> =
        ResponseEntity.ok(readingService.update(id, request))

    @DeleteMapping("/{id}")
    fun deleteReading(@PathVariable id: Long): ResponseEntity<Void> {
        readingService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(
        BookClubReadingNotFoundException::class,
        BookClubNotFoundException::class,
        BookNotFoundException::class
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: RuntimeException): Map<String, String?> =
        mapOf("error" to ex.message)

    @ExceptionHandler(ReadingDateRangeConflictException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDateRangeConflict(ex: ReadingDateRangeConflictException): Map<String, String?> =
        mapOf("error" to ex.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): Map<String, Any> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        return mapOf("errors" to errors)
    }
}
