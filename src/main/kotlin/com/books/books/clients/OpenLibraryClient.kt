package com.books.books.clients

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * Minimal client for the public Open Library Search API
 * (https://openlibrary.org/search.json). No API key is required.
 */
@Component
class OpenLibraryClient(builder: RestClient.Builder) {

    private val restClient = builder.baseUrl(BASE_URL).build()

    /**
     * Looks up a single book by title and returns the best (first) match, or
     * null when nothing is found.
     */
    fun searchByTitle(title: String): OpenLibraryBook? =
        restClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/search.json")
                    .queryParam("title", title)
                    .queryParam("limit", 1)
                    .queryParam("fields", FIELDS)
                    .build()
            }
            .retrieve()
            .body(OpenLibrarySearchResponse::class.java)
            ?.docs
            ?.firstOrNull()

    companion object {
        private const val BASE_URL = "https://openlibrary.org"

        /** Only request the fields the seeder maps, to keep responses small. */
        private const val FIELDS =
            "title,author_name,publisher,first_publish_year,number_of_pages_median,language,subject"
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenLibrarySearchResponse(
    val docs: List<OpenLibraryBook> = emptyList()
)

/**
 * A single search result. Most fields are optional because Open Library only
 * returns what it knows about a given work.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenLibraryBook(
    val title: String,

    @JsonProperty("author_name")
    val authorNames: List<String> = emptyList(),

    val publisher: List<String> = emptyList(),

    @JsonProperty("first_publish_year")
    val firstPublishYear: Int? = null,

    @JsonProperty("number_of_pages_median")
    val pageCount: Int? = null,

    /** MARC21 language codes (e.g. "eng", "fre"), not ISO 639-1. */
    val language: List<String> = emptyList(),

    val subject: List<String> = emptyList()
)
