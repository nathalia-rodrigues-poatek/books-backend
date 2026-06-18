package com.books.books.models

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "books")
data class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @ManyToOne(optional = false)
    @JoinColumn(name = "genre_id", nullable = false)
    val genre: Genre,

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    val author: Author,

    @ManyToOne(optional = false)
    @JoinColumn(name = "publisher_id", nullable = false)
    val publisher: Publisher,

    @Column(name = "published_date")
    val publishedDate: LocalDate? = null,

    @Column(name = "page_count")
    val pageCount: Int? = null,

    @Column
    val language: String? = null
)
