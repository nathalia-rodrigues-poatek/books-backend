package com.books.books.config

import com.books.books.clients.OpenLibraryBook
import com.books.books.clients.OpenLibraryClient
import com.books.books.models.Author
import com.books.books.models.Book
import com.books.books.models.Genre
import com.books.books.models.Publisher
import com.books.books.repositories.AuthorRepository
import com.books.books.repositories.BookRepository
import com.books.books.repositories.GenreRepository
import com.books.books.repositories.PublisherRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Configuration
import java.time.LocalDate

/**
 * Seeds sample books on startup so the read-only book endpoints have data to
 * serve. Metadata is fetched from the public Open Library Search API; any title
 * that cannot be fetched (no match or network failure) is skipped so seeding
 * never blocks startup. Runs only when the books table is empty (e.g. fresh
 * in-memory H2 on each boot).
 */
@Configuration
class BookDataSeeder(
    private val openLibraryClient: OpenLibraryClient,
    private val bookRepository: BookRepository,
    private val genreRepository: GenreRepository,
    private val authorRepository: AuthorRepository,
    private val publisherRepository: PublisherRepository
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(BookDataSeeder::class.java)

    // Reuse genres/authors/publishers across books so shared values aren't duplicated.
    private val genres = mutableMapOf<String, Genre>()
    private val authors = mutableMapOf<String, Author>()
    private val publishers = mutableMapOf<String, Publisher>()

    override fun run(vararg args: String?) {
        if (bookRepository.count() > 0) {
            return
        }

        TITLES.forEach { title ->
            try {
                seedBook(title)
            } catch (ex: Exception) {
                log.warn("Skipping seed for \"{}\": {}", title, ex.message)
            }
        }
    }

    private fun seedBook(title: String) {
        val data = openLibraryClient.searchByTitle(title)
        if (data == null) {
            log.warn("No Open Library result for \"{}\", skipping", title)
            return
        }

        val book = bookRepository.save(
            Book(
                name = data.title,
                genre = genreFor(data),
                author = authorFor(data),
                publisher = publisherFor(data),
                publishedDate = data.firstPublishYear?.let { LocalDate.of(it, 1, 1) },
                pageCount = data.pageCount,
                language = data.language.firstOrNull()?.let(::toIsoLanguage)
            )
        )
        log.info("Seeded \"{}\" from Open Library", book.name)
    }

    private fun genreFor(data: OpenLibraryBook): Genre {
        val name = data.subject.firstOrNull()?.take(255) ?: "Uncategorized"
        return genres.getOrPut(name) { genreRepository.save(Genre(name = name)) }
    }

    private fun authorFor(data: OpenLibraryBook): Author {
        val name = data.authorNames.firstOrNull() ?: "Unknown"
        return authors.getOrPut(name) { authorRepository.save(Author(name = name)) }
    }

    private fun publisherFor(data: OpenLibraryBook): Publisher {
        val name = data.publisher.firstOrNull() ?: "Unknown"
        return publishers.getOrPut(name) { publisherRepository.save(Publisher(name = name)) }
    }

    /** Open Library reports MARC21 codes; map the common ones to ISO 639-1. */
    private fun toIsoLanguage(marc: String): String = when (marc) {
        "eng" -> "en"
        "fre", "fra" -> "fr"
        "ger", "deu" -> "de"
        "spa" -> "es"
        "ita" -> "it"
        "por" -> "pt"
        else -> marc
    }

    companion object {
        private val TITLES = listOf(
            "The Hobbit",
            "Dune",
            "1984",
            "Pride and Prejudice",
            "Fahrenheit 451",
            "The Name of the Wind"
        )
    }
}
