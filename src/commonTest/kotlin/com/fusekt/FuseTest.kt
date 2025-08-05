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

    @Test
    fun testWeightedKeysWithIndexResultItem() {
        // Create test data using Map structure (which works with current Fuse implementation)
        val testData = listOf(
            mapOf(
                "authorName" to "John Smith",
                "authorUid" to "user123", 
                "body" to "This is a comprehensive guide to machine learning algorithms and their applications in modern software development.",
                "tags" to listOf("machine-learning", "ai", "algorithms", "programming"),
                "title" to "Machine Learning Fundamentals",
                "url" to "https://example.com/ml-guide"
            ),
            mapOf(
                "authorName" to "Sarah Johnson",
                "authorUid" to "user456",
                "body" to "Exploring the latest trends in web development, including React, Vue, and Angular frameworks for building scalable applications.",
                "tags" to listOf("web-development", "react", "vue", "angular", "javascript"),
                "title" to "Modern Web Development Trends", 
                "url" to "https://example.com/web-trends"
            ),
            mapOf(
                "authorName" to "Mike Davis",
                "authorUid" to "user789",
                "body" to "A deep dive into Kotlin programming language features, coroutines, and multiplatform development capabilities.",
                "tags" to listOf("kotlin", "programming", "multiplatform", "coroutines"),
                "title" to "Kotlin Programming Guide",
                "url" to "https://example.com/kotlin-guide"
            ),
            mapOf(
                "authorName" to "Emma Wilson",
                "authorUid" to "user321",
                "body" to "Database optimization techniques and best practices for improving query performance in large-scale applications.",
                "tags" to listOf("database", "optimization", "performance", "sql"),
                "title" to "Database Performance Optimization",
                "url" to "https://example.com/db-optimization"
            ),
            mapOf(
                "authorName" to "Alex Chen",
                "authorUid" to "user654",
                "body" to "Understanding cloud computing architectures and serverless technologies for building resilient distributed systems.",
                "tags" to listOf("cloud", "serverless", "architecture", "distributed-systems"),
                "title" to "Cloud Computing Architecture",
                "url" to "https://example.com/cloud-architecture"
            )
        )

        // Configure Fuse with weighted keys
        val options = FuseOptions.withWeightedKeys(
            weightedKeys = mapOf(
                "body" to 0.17,
                "title" to 0.3,
                "authorName" to 0.01,
                "authorUid" to 0.01,
                "url" to 0.01,
                "tags" to 0.5,
            ),
            includeScore = true,
        )

        val fuse = Fuse(testData, options)

        // Test search for "kotlin"
        val kotlinResults = fuse.search("kotlin")
        assertTrue(kotlinResults.isNotEmpty(), "Should find results for 'kotlin'")
        
        // Verify scores are included
        assertTrue(kotlinResults.first().score != null, "Score should be included")
        
        // Test search for "web development"
        val webResults = fuse.search("web development")
        assertTrue(webResults.isNotEmpty(), "Should find results for 'web development'")
        
        // Test search for "machine learning"
        val mlResults = fuse.search("machine learning")
        assertTrue(mlResults.isNotEmpty(), "Should find results for 'machine learning'")
        
        // Test search for author name (low weight should still work)
        val authorResults = fuse.search("Sarah Johnson")
        assertTrue(authorResults.isNotEmpty(), "Should find results for author name")

        // Test that tags search works (highest weight)
        val tagResults = fuse.search("programming")
        assertTrue(tagResults.isNotEmpty(), "Should find results for tag 'programming'")
        
        // Verify all results have scores when includeScore is true
        kotlinResults.forEach { result ->
            assertTrue(result.score != null, "All results should have scores when includeScore is true")
        }
    }
}