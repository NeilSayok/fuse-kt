package com.fusekt

import com.fusekt.core.FuseOptions
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Test to verify that the JVM-specific Fuse implementation is properly generic
 */
class GenericFuseTest {

    @Test
    fun testJvmGenericFuseWithStrings() {
        val strings = listOf("kotlin", "java", "scala", "groovy")
        val fuse = Fuse<String>(strings)
        
        val results = fuse.search("kot")
        assertTrue(results.isNotEmpty(), "Should find matches for 'kot'")
        
        // Verify type safety - results should be of type FormattedResult<String>
        results.forEach { result ->
            val item: String = result.item // This should compile without casting
            assertTrue(item.isNotEmpty(), "Item should be a non-empty string")
        }
    }

    @Test 
    fun testJvmGenericFuseWithMaps() {
        val data = listOf(
            mapOf("name" to "Alice", "age" to 30),
            mapOf("name" to "Bob", "age" to 25),
            mapOf("name" to "Charlie", "age" to 35)
        )
        
        val options = FuseOptions(keys = listOf("name"))
        val fuse = Fuse<Map<String, Any>>(data, options)
        
        val results = fuse.search("Ali")
        assertTrue(results.isNotEmpty(), "Should find matches for 'Ali'")
        
        // Verify type safety - results should be of type FormattedResult<Map<String, Any>>
        results.forEach { result ->
            val item: Map<String, Any> = result.item // This should compile without casting
            assertTrue(item.containsKey("name"), "Item should have a 'name' key")
        }
    }

    @Test
    fun testGenericTypeInferenceWorks() {
        // Test that we can create Fuse without explicit type parameters
        val books = listOf("1984", "Brave New World", "Fahrenheit 451")
        val fuse = Fuse(books) // Type should be inferred as Fuse<String>
        
        val results = fuse.search("451")
        assertTrue(results.isNotEmpty(), "Should find matches")
        
        // This verifies that the generic type was properly inferred
        val firstItem: String = results.first().item
        assertTrue(firstItem.contains("451"))
    }
}