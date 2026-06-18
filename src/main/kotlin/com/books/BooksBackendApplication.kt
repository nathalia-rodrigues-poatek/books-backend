package com.books

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BooksBackendApplication

fun main(args: Array<String>) {
    runApplication<BooksBackendApplication>(*args)
}
