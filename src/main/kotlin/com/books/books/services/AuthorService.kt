package com.books.books.services

import com.books.books.dtos.AuthorResponse
import com.books.books.dtos.CreateAuthorRequest
import com.books.books.dtos.UpdateAuthorRequest
import com.books.books.models.Author
import com.books.books.repositories.AuthorRepository
import org.springframework.stereotype.Service

@Service
class AuthorService(
    private val authorRepository: AuthorRepository
) {

    fun findAll(): List<AuthorResponse> =
        authorRepository.findAll().map(AuthorResponse::from)

    fun findById(id: Long): AuthorResponse =
        authorRepository.findById(id)
            .map(AuthorResponse::from)
            .orElseThrow { AuthorNotFoundException(id) }

    fun create(request: CreateAuthorRequest): AuthorResponse =
        AuthorResponse.from(authorRepository.save(Author(name = request.name)))

    fun update(id: Long, request: UpdateAuthorRequest): AuthorResponse {
        val author = authorRepository.findById(id).orElseThrow { AuthorNotFoundException(id) }
        return AuthorResponse.from(authorRepository.save(author.copy(name = request.name)))
    }

    fun delete(id: Long) {
        if (!authorRepository.existsById(id)) {
            throw AuthorNotFoundException(id)
        }
        authorRepository.deleteById(id)
    }
}

class AuthorNotFoundException(id: Long) : RuntimeException("Author with id $id not found")
