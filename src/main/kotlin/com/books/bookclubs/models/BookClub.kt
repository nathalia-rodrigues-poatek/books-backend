package com.books.bookclubs.models

import com.books.users.models.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "book_clubs")
data class BookClub(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @ManyToOne(optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    val admin: User,

    @ManyToMany
    @JoinTable(
        name = "book_club_members",
        joinColumns = [JoinColumn(name = "book_club_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val members: List<User> = emptyList(),

    @Column
    val image: String? = null,

    @Column(length = 2000)
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val visibility: Visibility = Visibility.PUBLIC
)
