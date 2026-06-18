package com.books.books.controllers

import com.books.books.dtos.CreatePublisherRequest
import com.books.books.dtos.PublisherResponse
import com.books.books.dtos.UpdatePublisherRequest
import com.books.books.services.PublisherNotFoundException
import com.books.books.services.PublisherService
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
@RequestMapping("/api/publishers")
class PublisherController(
    private val publisherService: PublisherService
) {

    @GetMapping
    fun getAllPublishers(): ResponseEntity<List<PublisherResponse>> =
        ResponseEntity.ok(publisherService.findAll())

    @GetMapping("/{id}")
    fun getPublisherById(@PathVariable id: Long): ResponseEntity<PublisherResponse> =
        ResponseEntity.ok(publisherService.findById(id))

    @PostMapping
    fun createPublisher(@Valid @RequestBody request: CreatePublisherRequest): ResponseEntity<PublisherResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(publisherService.create(request))

    @PutMapping("/{id}")
    fun updatePublisher(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePublisherRequest
    ): ResponseEntity<PublisherResponse> =
        ResponseEntity.ok(publisherService.update(id, request))

    @DeleteMapping("/{id}")
    fun deletePublisher(@PathVariable id: Long): ResponseEntity<Void> {
        publisherService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(PublisherNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handlePublisherNotFound(ex: PublisherNotFoundException): Map<String, String?> =
        mapOf("error" to ex.message)

    @ExceptionHandler(DataIntegrityViolationException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): Map<String, String?> =
        mapOf("error" to "Publisher is still referenced by one or more books")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): Map<String, Any> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        return mapOf("errors" to errors)
    }
}
