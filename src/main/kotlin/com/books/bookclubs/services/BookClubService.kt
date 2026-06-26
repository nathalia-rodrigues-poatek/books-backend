package com.books.bookclubs.services

import com.books.bookclubs.dtos.BookClubRequest
import com.books.bookclubs.dtos.BookClubResponse
import com.books.bookclubs.models.BookClub
import com.books.bookclubs.repositories.BookClubRepository
import com.books.users.models.User
import com.books.users.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class BookClubService(
    private val bookClubRepository: BookClubRepository,
    private val userRepository: UserRepository
) {

    fun findAll(): List<BookClubResponse> =
        bookClubRepository.findAll().map(BookClubResponse::from)

    fun findById(id: Long): BookClubResponse =
        bookClubRepository.findById(id)
            .map(BookClubResponse::from)
            .orElseThrow { BookClubNotFoundException(id) }

    fun create(request: BookClubRequest): BookClubResponse {
        val club = BookClub(
            name = request.name,
            admin = resolveUser(request.adminId!!),
            members = resolveMembers(request.memberIds),
            image = request.image,
            description = request.description,
            visibility = request.visibility!!
        )
        return BookClubResponse.from(bookClubRepository.save(club))
    }

    fun update(id: Long, request: BookClubRequest): BookClubResponse {
        val club = bookClubRepository.findById(id).orElseThrow { BookClubNotFoundException(id) }
        val updated = club.copy(
            name = request.name,
            admin = resolveUser(request.adminId!!),
            members = resolveMembers(request.memberIds),
            image = request.image,
            description = request.description,
            visibility = request.visibility!!
        )
        return BookClubResponse.from(bookClubRepository.save(updated))
    }

    fun delete(id: Long) {
        // TODO: turn this into a soft delete (flag the row as deleted instead of
        //  removing it), like the users domain does.
        if (!bookClubRepository.existsById(id)) {
            throw BookClubNotFoundException(id)
        }
        bookClubRepository.deleteById(id)
    }

    private fun resolveMembers(memberIds: List<Long>): List<User> =
        memberIds.distinct().map(::resolveUser)

    private fun resolveUser(userId: Long): User =
        userRepository.findByIdAndDeletedFalse(userId).orElseThrow { BookClubUserNotFoundException(userId) }
}

class BookClubNotFoundException(id: Long) : RuntimeException("Book club with id $id not found")

class BookClubUserNotFoundException(id: Long) : RuntimeException("User with id $id not found")
