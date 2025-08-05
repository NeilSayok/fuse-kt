package com.fusekt.core

import com.fusekt.transform.FormattedResult
import com.fusekt.transform.transformMatches
import com.fusekt.transform.transformScore
import kotlin.math.pow

fun <T> format(
    results: List<FuseResult<T>>,
    docs: List<T>,
    includeMatches: Boolean = false,
    includeScore: Boolean = false
): List<FormattedResult<T>> {
    val transformers = mutableListOf<(FuseResult<T>, FormattedResult<T>) -> FormattedResult<T>>()

    if (includeMatches) transformers.add(::transformMatches)
    if (includeScore) transformers.add(::transformScore)

    return results.map { result ->
        val idx = result.idx

        var data = FormattedResult<T>(
            item = docs[idx],
            refIndex = idx
        )

        transformers.forEach { transformer ->
            data = transformer(result, data)
        }

        data
    }
}

fun <T> computeScore(
    results: MutableList<FuseResult<T>>,
    ignoreFieldNorm: Boolean = false,
    keyStore: com.fusekt.tools.KeyStore? = null
) {
    results.forEachIndexed { index, result ->
        var totalScore = 1.0

        result.matches.forEach { match ->
            // Try to get the actual weight from keyStore using the key, fallback to 1.0  
            val weight = keyStore?.get(match.key ?: "")?.weight ?: 1.0

            totalScore *= (if (match.score == 0.0 && weight > 0) Double.MIN_VALUE else match.score).pow(
                weight * (if (ignoreFieldNorm) 1.0 else match.norm ?: 1.0)
            )
        }

        // Update the result in place
        results[index] = FuseResult<T>(
            item = result.item,
            idx = result.idx,
            score = totalScore,
            matches = result.matches
        )
    }
}