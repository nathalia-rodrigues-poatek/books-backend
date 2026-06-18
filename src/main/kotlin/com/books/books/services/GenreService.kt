package com.books.books.services

import com.books.books.dtos.CreateGenreRequest
import com.books.books.dtos.GenreResponse
import com.books.books.dtos.UpdateGenreRequest
import com.books.books.models.Genre
import com.books.books.repositories.GenreRepository
import org.springframework.stereotype.Service

@Service
class GenreService(
    private val genreRepository: GenreRepository
) {

    fun findAll(): List<GenreResponse> =
        genreRepository.findAll().map(GenreResponse::from)

    fun findById(id: Long): GenreResponse =
        genreRepository.findById(id)
            .map(GenreResponse::from)
            .orElseThrow { GenreNotFoundException(id) }

    fun create(request: CreateGenreRequest): GenreResponse =
        GenreResponse.from(genreRepository.save(Genre(name = request.name)))

    fun update(id: Long, request: UpdateGenreRequest): GenreResponse {
        val genre = genreRepository.findById(id).orElseThrow { GenreNotFoundException(id) }
        return GenreResponse.from(genreRepository.save(genre.copy(name = request.name)))
    }

    fun delete(id: Long) {
        if (!genreRepository.existsById(id)) {
            throw GenreNotFoundException(id)
        }
        genreRepository.deleteById(id)
    }
}

class GenreNotFoundException(id: Long) : RuntimeException("Genre with id $id not found")
