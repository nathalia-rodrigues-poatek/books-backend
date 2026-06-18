package com.books.books.config

import com.books.books.models.Author
import com.books.books.models.Book
import com.books.books.models.Genre
import com.books.books.models.Publisher
import com.books.books.repositories.AuthorRepository
import com.books.books.repositories.BookRepository
import com.books.books.repositories.GenreRepository
import com.books.books.repositories.PublisherRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Configuration
import java.time.LocalDate

/**
 * Seeds sample genres, authors, publishers and books on startup so the
 * read-only book endpoints have data to serve. Runs only when the books
 * table is empty (e.g. fresh in-memory H2 on each boot).
 */
@Configuration
class BookDataSeeder(
    private val bookRepository: BookRepository,
    private val genreRepository: GenreRepository,
    private val authorRepository: AuthorRepository,
    private val publisherRepository: PublisherRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (bookRepository.count() > 0) {
            return
        }

        val fantasy = genreRepository.save(Genre(name = "Fantasy"))
        val sciFi = genreRepository.save(Genre(name = "Science Fiction"))

        val tolkien = authorRepository.save(Author(name = "J.R.R. Tolkien"))
        val herbert = authorRepository.save(Author(name = "Frank Herbert"))

        val allenLane = publisherRepository.save(
            Publisher(name = "Allen & Unwin", website = "https://www.allenandunwin.com")
        )
        val chilton = publisherRepository.save(
            Publisher(name = "Chilton Books", website = null)
        )

        bookRepository.save(
            Book(
                name = "The Hobbit",
                genre = fantasy,
                author = tolkien,
                publisher = allenLane,
                publishedDate = LocalDate.of(1937, 9, 21),
                pageCount = 310,
                language = "en"
            )
        )
        bookRepository.save(
            Book(
                name = "Dune",
                genre = sciFi,
                author = herbert,
                publisher = chilton,
                publishedDate = LocalDate.of(1965, 8, 1),
                pageCount = 412,
                language = "en"
            )
        )
    }
}
