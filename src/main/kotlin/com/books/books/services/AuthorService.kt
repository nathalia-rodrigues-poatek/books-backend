package com.books.books.services

import com.books.books.dtos.AuthorRequest
import com.books.books.dtos.AuthorResponse
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

    fun create(request: AuthorRequest): AuthorResponse =
        AuthorResponse.from(authorRepository.save(Author(name = request.name)))

    fun update(id: Long, request: AuthorRequest): AuthorResponse {
        val author = authorRepository.findById(id).orElseThrow { AuthorNotFoundException(id) }
        return AuthorResponse.from(authorRepository.save(author.copy(name = request.name)))
    }

    fun delete(id: Long) {
        // TODO: turn this into a soft delete (flag the row as deleted instead of
        //  removing it), like the users domain does. Applies to all deletes in the books domain.
        if (!authorRepository.existsById(id)) {
            throw AuthorNotFoundException(id)
        }
        authorRepository.deleteById(id)
    }
}

class AuthorNotFoundException(id: Long) : RuntimeException("Author with id $id not found")
