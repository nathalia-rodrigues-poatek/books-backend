package com.books.books.services

import com.books.books.dtos.PublisherRequest
import com.books.books.dtos.PublisherResponse
import com.books.books.models.Publisher
import com.books.books.repositories.PublisherRepository
import org.springframework.stereotype.Service

@Service
class PublisherService(
    private val publisherRepository: PublisherRepository
) {

    fun findAll(): List<PublisherResponse> =
        publisherRepository.findAll().map(PublisherResponse::from)

    fun findById(id: Long): PublisherResponse =
        publisherRepository.findById(id)
            .map(PublisherResponse::from)
            .orElseThrow { PublisherNotFoundException(id) }

    fun create(request: PublisherRequest): PublisherResponse =
        PublisherResponse.from(
            publisherRepository.save(Publisher(name = request.name, website = request.website))
        )

    fun update(id: Long, request: PublisherRequest): PublisherResponse {
        val publisher = publisherRepository.findById(id).orElseThrow { PublisherNotFoundException(id) }
        return PublisherResponse.from(
            publisherRepository.save(publisher.copy(name = request.name, website = request.website))
        )
    }

    fun delete(id: Long) {
        // TODO: turn this into a soft delete (flag the row as deleted instead of
        //  removing it), like the users domain does. Applies to all deletes in the books domain.
        if (!publisherRepository.existsById(id)) {
            throw PublisherNotFoundException(id)
        }
        publisherRepository.deleteById(id)
    }
}

class PublisherNotFoundException(id: Long) : RuntimeException("Publisher with id $id not found")
