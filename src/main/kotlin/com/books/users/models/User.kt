package com.books.users.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false, unique = true)
    val token: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val deleted: Boolean = false,

    @Column(nullable = false)
    val blocked: Boolean = false,

    @Column(name = "deleted_date")
    val deletedDate: LocalDateTime? = null,

    @Column(name = "blocked_date")
    val blockedDate: LocalDateTime? = null
)