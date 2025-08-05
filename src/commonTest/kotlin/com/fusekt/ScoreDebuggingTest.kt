package com.fusekt

import com.fusekt.core.FuseOptions
import kotlin.test.Test
import kotlin.test.assertTrue

class ScoreDebuggingTest {

    @Test
    fun debugScore() {
        val data = listOf(
            mapOf("name" to "John Doe")
        )
        
        val options = FuseOptions(
            keys = listOf("name"),
            includeScore = true,
            includeMatches = true
        )
        
        val fuse = Fuse(data, options)
        val results = fuse.search("John")
        
        assertTrue(results.isNotEmpty(), "Should have results")
        val firstResult = results.first()
        
        // Verify that the score is no longer 0.0
        assertTrue(firstResult.score != null, "Score should not be null")
        assertTrue(firstResult.score!! > 0.0, "Score should be greater than 0.0")
        
        // Verify that we have matches
        assertTrue(firstResult.matches?.isNotEmpty() == true, "Should have matches")
    }
}