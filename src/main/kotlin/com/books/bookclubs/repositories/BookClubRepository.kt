package com.books.bookclubs.repositories

import com.books.bookclubs.models.BookClub
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BookClubRepository : JpaRepository<BookClub, Long>
