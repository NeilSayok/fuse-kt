package com.fusekt

import com.fusekt.core.FuseOptions

fun main() {
    // Example usage of Fuse-kt
    println("Fuse-kt Library v${Fuse.version}")
    println("==========================")

    // Example 1: Simple string search
    val fruits = listOf("apple", "banana", "orange", "grape", "pineapple")
    val fuseStrings = Fuse(fruits)
    
    println("\n1. Simple string search:")
    println("Searching for 'app' in fruits:")
    val stringResults = fuseStrings.search("app")
    stringResults.forEach { result ->
        println("  - ${result.item} (score: ${result.score})")
    }

    // Example 2: Object search
    val books = listOf(
        mapOf("title" to "The Great Gatsby", "author" to "F. Scott Fitzgerald"),
        mapOf("title" to "To Kill a Mockingbird", "author" to "Harper Lee"),
        mapOf("title" to "1984", "author" to "George Orwell"),
        mapOf("title" to "Pride and Prejudice", "author" to "Jane Austen"),
        mapOf("title" to "The Catcher in the Rye", "author" to "J.D. Salinger")
    )

    val options = FuseOptions(
        keys = listOf("title", "author"),
        includeScore = true,
        includeMatches = true,
        threshold = 0.6
    )

    val fuseBooks = Fuse(books, options)
    
    println("\n2. Object search with keys:")
    println("Searching for 'gatsby' in books:")
    val bookResults = fuseBooks.search("gatsby")
    bookResults.forEach { result ->
        val book = result.item as Map<*, *>
        println("  - ${book["title"]} by ${book["author"]} (score: ${result.score})")
        if (result.matches.isNotEmpty()) {
            result.matches.forEach { match ->
                println("    Match: '${match.value}' in ${match.key}")
            }
        }
    }

    println("\n3. Search with lower threshold:")
    val strictOptions = options.copy(threshold = 0.3)
    val fuseStrict = Fuse(books, strictOptions)
    val strictResults = fuseStrict.search("orwell")
    strictResults.forEach { result ->
        val book = result.item as Map<*, *>
        println("  - ${book["title"]} by ${book["author"]} (score: ${result.score})")
    }

    println("\nFuse-kt conversion completed successfully!")
}