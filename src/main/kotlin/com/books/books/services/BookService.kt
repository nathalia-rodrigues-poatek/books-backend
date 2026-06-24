package com.books.books.services

import com.books.books.dtos.BookRequest
import com.books.books.dtos.BookResponse
import com.books.books.models.Author
import com.books.books.models.Book
import com.books.books.models.Genre
import com.books.books.models.Publisher
import com.books.books.repositories.AuthorRepository
import com.books.books.repositories.BookRepository
import com.books.books.repositories.GenreRepository
import com.books.books.repositories.PublisherRepository
import org.springframework.stereotype.Service

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val genreRepository: GenreRepository,
    private val authorRepository: AuthorRepository,
    private val publisherRepository: PublisherRepository
) {

    fun findAll(): List<BookResponse> =
        bookRepository.findAll().map(BookResponse::from)

    fun findById(id: Long): BookResponse =
        bookRepository.findById(id)
            .map(BookResponse::from)
            .orElseThrow { BookNotFoundException(id) }

    fun create(request: BookRequest): BookResponse {
        val book = Book(
            name = request.name,
            genre = resolveGenre(request.genreId!!),
            author = resolveAuthor(request.authorId!!),
            publisher = resolvePublisher(request.publisherId!!),
            publishedDate = request.publishedDate,
            pageCount = request.pageCount,
            language = request.language
        )
        return BookResponse.from(bookRepository.save(book))
    }

    fun update(id: Long, request: BookRequest): BookResponse {
        val book = bookRepository.findById(id).orElseThrow { BookNotFoundException(id) }
        val updated = book.copy(
            name = request.name,
            genre = resolveGenre(request.genreId!!),
            author = resolveAuthor(request.authorId!!),
            publisher = resolvePublisher(request.publisherId!!),
            publishedDate = request.publishedDate,
            pageCount = request.pageCount,
            language = request.language
        )
        return BookResponse.from(bookRepository.save(updated))
    }

    fun delete(id: Long) {
        // TODO: turn this into a soft delete (flag the row as deleted instead of
        //  removing it), like the users domain does. Applies to all deletes in the books domain.
        if (!bookRepository.existsById(id)) {
            throw BookNotFoundException(id)
        }
        bookRepository.deleteById(id)
    }

    private fun resolveGenre(id: Long): Genre =
        genreRepository.findById(id).orElseThrow { GenreNotFoundException(id) }

    private fun resolveAuthor(id: Long): Author =
        authorRepository.findById(id).orElseThrow { AuthorNotFoundException(id) }

    private fun resolvePublisher(id: Long): Publisher =
        publisherRepository.findById(id).orElseThrow { PublisherNotFoundException(id) }
}

class BookNotFoundException(id: Long) : RuntimeException("Book with id $id not found")
