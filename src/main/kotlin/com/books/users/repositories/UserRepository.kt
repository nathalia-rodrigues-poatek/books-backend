package com.books.users.repositories

import com.books.users.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByDeletedFalse(): List<User>
    fun findByIdAndDeletedFalse(id: Long): Optional<User>
    fun findByEmailAndDeletedFalse(email: String): Optional<User>
}