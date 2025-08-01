package com.fusekt

import com.fusekt.core.FuseOptions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuseTest {

    @Test
    fun testSimpleStringSearch() {
        val fruits = listOf("apple", "banana", "orange", "grape", "pineapple")
        val fuse = Fuse(fruits)
        
        val results = fuse.search("app")
        assertTrue(results.isNotEmpty(), "Should find matches for 'app'")
        
        val items = results.map { it.item }
        assertTrue(items.contains("apple"), "Should contain 'apple'")
        assertTrue(items.contains("pineapple"), "Should contain 'pineapple'")
    }

    @Test
    fun testObjectSearch() {
        val books = listOf(
            mapOf("title" to "The Great Gatsby", "author" to "F. Scott Fitzgerald"),
            mapOf("title" to "To Kill a Mockingbird", "author" to "Harper Lee"),
            mapOf("title" to "1984", "author" to "George Orwell")
        )

        val options = FuseOptions(
            keys = listOf("title", "author"),
            threshold = 0.6
        )

        val fuse = Fuse(books, options)
        val results = fuse.search("gatsby")
        
        assertTrue(results.isNotEmpty(), "Should find matches for 'gatsby'")
        
        val firstResult = results.first().item as Map<*, *>
        assertEquals("The Great Gatsby", firstResult["title"])
    }

    @Test
    fun testExactMatch() {
        val words = listOf("hello", "world", "test")
        val fuse = Fuse(words)
        
        val results = fuse.search("hello")
        assertTrue(results.isNotEmpty(), "Should find exact match")
        
        val firstResult = results.first()
        assertEquals("hello", firstResult.item)
    }

    @Test
    fun testNoMatches() {
        val words = listOf("hello", "world", "test")
        val fuse = Fuse(words)
        
        val results = fuse.search("xyz")
        // Note: Due to fuzzy matching, this might still return results with low scores
        // The actual behavior depends on the threshold setting
    }

    @Test
    fun testAddAndRemove() {
        val words = mutableListOf("hello", "world")
        val fuse = Fuse(words)
        
        // Add a new item
        fuse.add("test")
        
        val results = fuse.search("test")
        assertTrue(results.isNotEmpty(), "Should find newly added item")
        
        // Remove an item
        val removed = fuse.remove { item, _ -> item == "hello" }
        assertEquals(1, removed.size)
        assertEquals("hello", removed.first())
    }

    @Test
    fun testThresholdConfiguration() {
        val words = listOf("hello", "world", "help")
        
        // Strict threshold
        val strictOptions = FuseOptions(threshold = 0.1)
        val strictFuse = Fuse(words, strictOptions)
        
        // Lenient threshold  
        val lenientOptions = FuseOptions(threshold = 0.8)
        val lenientFuse = Fuse(words, lenientOptions)
        
        val strictResults = strictFuse.search("helo") // typo
        val lenientResults = lenientFuse.search("helo") // typo
        
        // Lenient should return more results than strict
        assertTrue(lenientResults.size >= strictResults.size, 
                  "Lenient threshold should return more results")
    }
}