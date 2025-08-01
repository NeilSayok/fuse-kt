package com.fusekt.core

import com.fusekt.transform.FormattedResult
import com.fusekt.transform.transformMatches
import com.fusekt.transform.transformScore
import kotlin.math.pow

fun format(
    results: List<FuseResult>,
    docs: List<Any>,
    includeMatches: Boolean = false,
    includeScore: Boolean = false
): List<FormattedResult> {
    val transformers = mutableListOf<(FuseResult, FormattedResult) -> FormattedResult>()

    if (includeMatches) transformers.add(::transformMatches)
    if (includeScore) transformers.add(::transformScore)

    return results.map { result ->
        val idx = result.idx

        var data = FormattedResult(
            item = docs[idx],
            refIndex = idx
        )

        transformers.forEach { transformer ->
            data = transformer(result, data)
        }

        data
    }
}

fun computeScore(
    results: MutableList<FuseResult>,
    ignoreFieldNorm: Boolean = false
) {
    results.forEach { result ->
        var totalScore = 1.0

        result.matches.forEach { match ->
            val weight = match.key?.let { 1.0 } ?: 1.0 // Simplified weight handling

            totalScore *= (if (match.score == 0.0 && weight > 0) Double.MIN_VALUE else match.score).pow(
                weight * (if (ignoreFieldNorm) 1.0 else match.norm ?: 1.0)
            )
        }

        // Create new result with updated score
        val updatedResult = FuseResult(
            item = result.item,
            idx = result.idx,
            score = totalScore,
            matches = result.matches
        )
        
        // Update the result in place
        results[results.indexOf(result)] = updatedResult
    }
}