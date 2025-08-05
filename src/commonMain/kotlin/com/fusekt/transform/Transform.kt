package com.fusekt.transform

import com.fusekt.core.FuseResult
import com.fusekt.helpers.isDefined

/**
 * Formatted search result ready for consumption by applications.
 *
 * @param T The type of the items being searched
 * @property item The original data item that matched
 * @property refIndex Reference index in the original dataset
 * @property matches List of detailed match information
 * @property score Relevance score (if included in options)
 */
data class FormattedResult<T>(
    val item: T,
    val refIndex: Int,
    var matches: List<FormattedMatch> = emptyList(),
    var score: Double? = null
)

/**
 * Detailed information about a specific field match.
 *
 * @property indices Character ranges that matched within the text
 * @property value The actual text value that contained the match
 * @property key The field name or key that was matched
 * @property refIndex Reference index for array elements
 */
data class FormattedMatch(
    val indices: List<IntRange>,
    val value: String,
    val key: Any? = null,
    val refIndex: Int? = null
)

/**
 * Transforms raw search matches into formatted results.
 *
 * @param T The type of the items being searched
 * @param result Raw search result from the search algorithm
 * @param data The formatted result to populate with match data
 * @return Updated FormattedResult with processed match information
 */
fun <T> transformMatches(result: FuseResult<T>, data: FormattedResult<T>): FormattedResult<T> {
    val matches = result.matches
    val formattedMatches = mutableListOf<FormattedMatch>()

    if (!isDefined(matches)) {
        return data.copy(matches = formattedMatches)
    }

    matches.forEach { match ->
        if (match.indices.isEmpty()) {
            return@forEach
        }

        val formattedMatch = FormattedMatch(
            indices = match.indices,
            value = match.value,
            key = match.key,
            refIndex = match.idx
        )

        formattedMatches.add(formattedMatch)
    }

    return data.copy(matches = formattedMatches)
}

fun <T> transformScore(result: FuseResult<T>, data: FormattedResult<T>): FormattedResult<T> {
    return data.copy(score = result.score)
}