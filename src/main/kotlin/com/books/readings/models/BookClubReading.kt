package com.books.readings.models

import com.books.bookclubs.models.BookClub
import com.books.books.models.Book
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * A book a [BookClub] is reading over a given period. A club can read many
 * books and a book can be read by many clubs, each with its own schedule.
 */
@Entity
@Table(name = "book_club_readings")
data class BookClubReading(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_club_id", nullable = false)
    val bookClub: BookClub,

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    val book: Book,

    @Column(name = "start_date", nullable = false)
    val startDate: LocalDate,

    @Column(name = "end_date", nullable = false)
    val endDate: LocalDate,

    @Column(name = "meet_link")
    val meetLink: String? = null
)
