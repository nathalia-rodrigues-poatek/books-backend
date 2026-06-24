package com.books.books.models

import jakarta.persistence.*

@Entity
@Table(name = "authors")
data class Author(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String
)
