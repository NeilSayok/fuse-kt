package com.fusekt

import com.fusekt.core.FuseOptions
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertTrue

// Define the exact data structure you requested
@Serializable
data class IndexResultItem(
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("author_uid") val authorUid: String? = null,
    @SerialName("body") val body: String? = null,
    @SerialName("tags") val tags: List<String?>? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("url") val url: String? = null
)

typealias IndexResult = List<IndexResultItem>

class IndexResultItemTest {

    @Test
    fun testWeightedKeysWithSerializableIndexResultItem() {
        // Create test data with random content
        val testData: IndexResult = listOf(
            IndexResultItem(
                authorName = "John Smith",
                authorUid = "user123",
                body = "This is a comprehensive guide to machine learning algorithms and their applications in modern software development.",
                tags = listOf("machine-learning", "ai", "algorithms", "programming"),
                title = "Machine Learning Fundamentals",
                url = "https://example.com/ml-guide"
            ),
            IndexResultItem(
                authorName = "Sarah Johnson",
                authorUid = "user456",
                body = "Exploring the latest trends in web development, including React, Vue, and Angular frameworks for building scalable applications.",
                tags = listOf("web-development", "react", "vue", "angular", "javascript"),
                title = "Modern Web Development Trends",
                url = "https://example.com/web-trends"
            ),
            IndexResultItem(
                authorName = "Mike Davis",
                authorUid = "user789",
                body = "A deep dive into Kotlin programming language features, coroutines, and multiplatform development capabilities.",
                tags = listOf("kotlin", "programming", "multiplatform", "coroutines"),
                title = "Kotlin Programming Guide",
                url = "https://example.com/kotlin-guide"
            ),
            IndexResultItem(
                authorName = "Emma Wilson",
                authorUid = "user321",
                body = "Database optimization techniques and best practices for improving query performance in large-scale applications.",
                tags = listOf("database", "optimization", "performance", "sql"),
                title = "Database Performance Optimization",
                url = "https://example.com/db-optimization"
            ),
            IndexResultItem(
                authorName = "Alex Chen",
                authorUid = "user654",
                body = "Understanding cloud computing architectures and serverless technologies for building resilient distributed systems.",
                tags = listOf("cloud", "serverless", "architecture", "distributed-systems"),
                title = "Cloud Computing Architecture",
                url = "https://example.com/cloud-architecture"
            )
        )

        // For custom data classes, we need to convert to a format Fuse can work with
        // Convert IndexResultItem to Map for Fuse compatibility
        val fuseData = testData.map { item ->
            mapOf(
                "author_name" to item.authorName,
                "author_uid" to item.authorUid,
                "body" to item.body,
                "tags" to item.tags,
                "title" to item.title,
                "url" to item.url,
                "originalItem" to item  // Keep reference to original item
            )
        }

        // Configure Fuse with exact weighted keys you specified
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

        val fuse = Fuse(fuseData, options)

        // Test various search queries
        val kotlinResults = fuse.search("kotlin")
        assertTrue(kotlinResults.isNotEmpty(), "Should find results for 'kotlin'")
        
        val webResults = fuse.search("web development")
        assertTrue(webResults.isNotEmpty(), "Should find results for 'web development'")
        
        val mlResults = fuse.search("machine learning")
        assertTrue(mlResults.isNotEmpty(), "Should find results for 'machine learning'")
        
        val authorResults = fuse.search("Sarah Johnson")
        assertTrue(authorResults.isNotEmpty(), "Should find results for author name")

        val tagResults = fuse.search("programming")
        assertTrue(tagResults.isNotEmpty(), "Should find results for tag 'programming'")
        
        // Verify all results have scores when includeScore is true
        kotlinResults.forEach { result ->
            assertTrue(result.score != null, "All results should have scores when includeScore is true")
        }

        // Extract original IndexResultItem from search results
        fun extractOriginalItem(searchResult: Any): IndexResultItem {
            val map = searchResult as Map<*, *>
            return map["originalItem"] as IndexResultItem
        }

        // Print sample results for verification
        println("Kotlin search results:")
        kotlinResults.take(3).forEach { result ->
            val originalItem = extractOriginalItem(result.item)
            println("- ${originalItem.title} (score: ${result.score})")
        }

        // Test extracting original items
        val firstKotlinResult = extractOriginalItem(kotlinResults.first().item)
        assertTrue(firstKotlinResult.title != null, "Should have access to original IndexResultItem")
    }
}