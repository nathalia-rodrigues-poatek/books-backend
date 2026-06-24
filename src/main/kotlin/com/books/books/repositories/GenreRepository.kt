package com.books.books.repositories

import com.books.books.models.Genre
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GenreRepository : JpaRepository<Genre, Long>
