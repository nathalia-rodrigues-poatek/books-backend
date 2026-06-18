package com.books.books.controllers

import com.books.books.dtos.CreateGenreRequest
import com.books.books.dtos.GenreResponse
import com.books.books.dtos.UpdateGenreRequest
import com.books.books.services.GenreNotFoundException
import com.books.books.services.GenreService
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
@RequestMapping("/api/genres")
class GenreController(
    private val genreService: GenreService
) {

    @GetMapping
    fun getAllGenres(): ResponseEntity<List<GenreResponse>> =
        ResponseEntity.ok(genreService.findAll())

    @GetMapping("/{id}")
    fun getGenreById(@PathVariable id: Long): ResponseEntity<GenreResponse> =
        ResponseEntity.ok(genreService.findById(id))

    @PostMapping
    fun createGenre(@Valid @RequestBody request: CreateGenreRequest): ResponseEntity<GenreResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(genreService.create(request))

    @PutMapping("/{id}")
    fun updateGenre(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateGenreRequest
    ): ResponseEntity<GenreResponse> =
        ResponseEntity.ok(genreService.update(id, request))

    @DeleteMapping("/{id}")
    fun deleteGenre(@PathVariable id: Long): ResponseEntity<Void> {
        genreService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(GenreNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleGenreNotFound(ex: GenreNotFoundException): Map<String, String?> =
        mapOf("error" to ex.message)

    @ExceptionHandler(DataIntegrityViolationException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): Map<String, String?> =
        mapOf("error" to "Genre is still referenced by one or more books")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): Map<String, Any> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        return mapOf("errors" to errors)
    }
}
