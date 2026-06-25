package com.books.books.models

import jakarta.persistence.*

@Entity
@Table(name = "publishers")
data class Publisher(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column
    val website: String? = null
)
