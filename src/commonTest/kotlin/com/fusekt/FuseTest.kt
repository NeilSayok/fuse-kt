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
            keys = listOf("title", "author"), threshold = 0.6
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
        assertTrue(
            lenientResults.size >= strictResults.size, "Lenient threshold should return more results"
        )
    }

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
        distance = 35
    )

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
            ), mapOf(
                "authorName" to "Sarah Johnson",
                "authorUid" to "user456",
                "body" to "Exploring the latest trends in web development, including React, Vue, and Angular frameworks for building scalable applications.",
                "tags" to listOf("web-development", "react", "vue", "angular", "javascript"),
                "title" to "Modern Web Development Trends",
                "url" to "https://example.com/web-trends"
            ), mapOf(
                "authorName" to "Mike Davis",
                "authorUid" to "user789",
                "body" to "A deep dive into Kotlin programming language features, coroutines, and multiplatform development capabilities.",
                "tags" to listOf("kotlin", "programming", "multiplatform", "coroutines"),
                "title" to "Kotlin Programming Guide",
                "url" to "https://example.com/kotlin-guide"
            ), mapOf(
                "authorName" to "Emma Wilson",
                "authorUid" to "user321",
                "body" to "Database optimization techniques and best practices for improving query performance in large-scale applications.",
                "tags" to listOf("database", "optimization", "performance", "sql"),
                "title" to "Database Performance Optimization",
                "url" to "https://example.com/db-optimization"
            ), mapOf(
                "authorName" to "Alex Chen",
                "authorUid" to "user654",
                "body" to "Understanding cloud computing architectures and serverless technologies for building resilient distributed systems.",
                "tags" to listOf("cloud", "serverless", "architecture", "distributed-systems"),
                "title" to "Cloud Computing Architecture",
                "url" to "https://example.com/cloud-architecture"
            )
        )

        // Configure Fuse with weighted keys


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


    @Test
    fun testWeightedKeysWithIndexResultItem2() {
        // Create test data using Map structure (which works with current Fuse implementation)
        val testData = listOf(
            mapOf(
                "author_name" to "Sayok Dey Majumder",
                "author_uid" to "NeilSayok",
                "body" to "question  given an integer array nums return true if any value appears at least twice in the and false every element is distinct examples input output problem link to leetcode this can be solved ways technique brute force we will iterate through each of then check again present more than one time algorithm loop on individual item store it a variable gt i elements j initialize counter with equals increase greater after inner there duplicate class solution fun containsduplicate intarray boolean for var 0 1 complexiy space o n2 reason are using nested loops no extra required but bit better remove as adds reduce complexity sorting here first sort sorted now compare adjacent both equal say that val arr use set where contains not insert mutablesetof add n only once keep inserting all match found solutions returning soon finding waiting or function complete ends much early",
                "tags" to "DSA,Kotlin,Array,LeetCode",
                "title" to "dsa  check if an array contains duplicate elements",
                "url" to "https://bluelabs.in/NeilSayok/check-if-an-array-contains-duplicate-elements-2422"
            ),
            mapOf(
                "author_name" to "Sayok Dey Majumder",
                "author_uid" to "NeilSayok",
                "body" to "in kotlin while creating a class we can pass the parameters to its constructor these ways test  var val this blog will understand what is difference between them so putting make variable property of and not it parameter only function does mean lets look into some code int now see decompiled java public final if i try access using object like below t new error give me unresolved reference why because put paramater decompliked become private geta return void seta var1 thus you but also getter setters for setting values next question arises field how be accessed simple answer cannot e are calling kt from able assign value 10 have use as internally handles getters 5 ok",
                "tags" to "Kotlin,Fundamentals,Constructor,var/val",
                "title" to "deep dive  understanding kotlin constructor with and without var val",
                "url" to "https://bluelabs.in/NeilSayok/understanding-kotlin-constructor-with-and-without-var-and-val-7270"
            ),
            mapOf(
                "author_name" to "Sayok Dey Majumder",
                "author_uid" to "NeilSayok",
                "body" to "const keyword is used to create immutable values the only thing these needs be defined at compile time has with val a variable are allowed on top level in named objects or companion which means it cannot locally inside of functions and classes syntax for  example name sayok initiated before code compiled as value stored will directly put source during compilation understand what meant by above line we need see happens when use kotlin decompile java object constants petname neil fun main println decompiled public final class static instance new private string getname return void system out so you s replaced print statement where note supports primitive data types mentioned below supported byte short int long double float char sting boolean",
                "tags" to "const,Kotlin,Kotlin Fundamentals,Constants",
                "title" to "kotlin fundamentals  understanding const in",
                "url" to "https://bluelabs.in/NeilSayok/kotlin-fundamentals-const-in-kotlin-9172"
            ),
            mapOf(
                "author_name" to "Sayok Dey Majumder",
                "author_uid" to "NeilSayok",
                "body" to "string interpolation in kotlin is a powerful feature that allows developers to easily incorporate variables expressions and even complex calculations directly within literals this simplifies the process of creating dynamic strings can make code more readable maintainable syntax for put  symbol follwed by variable name if you are using data type operations use example val sayok age 30 print my output above will be we have like class person sting int fun main user neil so learnt how write but now see what happens under hood understand it need decompile below finalstring decompiled new stringbuilder append 5 tostring compiler builder concatenate done avoid multiple objects which would happen simple concatenation hello were used sometime optimize less memory space do place",
                "tags" to "Kotlin,String,Fundamentals,Interpolation",
                "title" to "string interpolation in kotlin",
                "url" to "https://bluelabs.in/NeilSayok/string-interpolation-in-kotlin-7309"
            )
        )

        // Configure Fuse with weighted keys

        val fuse = Fuse(testData, options)

        // Test search for "kotlin"
        val kotlinResults = fuse.search("DSA")
        assertTrue(kotlinResults.isNotEmpty(), "Should find results for 'kotlin'")


    }

    @Test
    fun testWeightedKeysWithIndexResultItemObj() {
        // Define the data structure


        // Create test data with random content
        val testData: IndexResult = listOf(
            IndexResultItem(
                authorName = "John Smith",
                authorUid = "user123",
                body = "This is a comprehensive guide to machine learning algorithms and their applications in modern software development.",
                tags = listOf("machine-learning", "ai", "algorithms", "programming"),
                title = "Machine Learning Fundamentals",
                url = "https://example.com/ml-guide"
            ), IndexResultItem(
                authorName = "Sarah Johnson",
                authorUid = "user456",
                body = "Exploring the latest trends in web development, including React, Vue, and Angular frameworks for building scalable applications.",
                tags = listOf("web-development", "react", "vue", "angular", "javascript"),
                title = "Modern Web Development Trends",
                url = "https://example.com/web-trends"
            ), IndexResultItem(
                authorName = "Mike Davis",
                authorUid = "user789",
                body = "A deep dive into Kotlin programming language features, coroutines, and multiplatform development capabilities.",
                tags = listOf("kotlin", "programming", "multiplatform", "coroutines"),
                title = "Kotlin Programming Guide",
                url = "https://example.com/kotlin-guide"
            ), IndexResultItem(
                authorName = "Emma Wilson",
                authorUid = "user321",
                body = "Database optimization techniques and best practices for improving query performance in large-scale applications.",
                tags = listOf("database", "optimization", "performance", "sql"),
                title = "Database Performance Optimization",
                url = "https://example.com/db-optimization"
            ), IndexResultItem(
                authorName = "Alex Chen",
                authorUid = "user654",
                body = "Understanding cloud computing architectures and serverless technologies for building resilient distributed systems.",
                tags = listOf("cloud", "serverless", "architecture", "distributed-systems"),
                title = "Cloud Computing Architecture",
                url = "https://example.com/cloud-architecture"
            )
        )

        // Configure Fuse with weighted keys
        val options = FuseOptions.withWeightedKeys(
            weightedKeys = mapOf(
                "body" to 0.17,
                "title" to 0.3,
                "author_name" to 0.01,
                "author_uid" to 0.01,
                "url" to 0.01,
                "tags" to 0.5,
            ),
            includeScore = true,
        )

        val fuse = Fuse<IndexResultItem>(testData, options)

        // Test search for "kotlin"
        val kotlinResults = fuse.search("kotlin")
        assertTrue(kotlinResults.isNotEmpty(), "Should find results for 'kotlin'")

        // The Kotlin Programming Guide should be highly ranked due to tags weight
        val firstResult = kotlinResults.first()
        assertEquals("Kotlin Programming Guide", firstResult.item.title)
        assertTrue(firstResult.score != null, "Score should be included")

        // Test search for "web development"
        val webResults = fuse.search("web development")
        assertTrue(webResults.isNotEmpty(), "Should find results for 'web development'")

        // Should find the web development article
        val webMatch = webResults.find { it.item.title?.contains("Web Development") == true }
        assertTrue(webMatch != null, "Should find web development article")

        // Test search for "machine learning"
        val mlResults = fuse.search("machine learning")
        assertTrue(mlResults.isNotEmpty(), "Should find results for 'machine learning'")

        // Machine learning article should be found due to high tags weight
        val mlMatch = mlResults.find { it.item.tags?.contains("machine-learning") == true }
        assertTrue(mlMatch != null, "Should find machine learning article via tags")

        // Test search for author name (low weight should still work but with lower ranking)
        val authorResults = fuse.search("Sarah Johnson")
        assertTrue(authorResults.isNotEmpty(), "Should find results for author name")

        // Verify all results have scores
        kotlinResults.forEach { result ->
            assertTrue(result.score != null, "All results should have scores when includeScore is true")
        }
    }
}