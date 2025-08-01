package com.fusekt.transform

import com.fusekt.core.FuseResult
import com.fusekt.helpers.isDefined

data class FormattedResult(
    val item: Any,
    val refIndex: Int,
    var matches: List<FormattedMatch> = emptyList(),
    var score: Double? = null
)

data class FormattedMatch(
    val indices: List<IntRange>,
    val value: String,
    val key: Any? = null,
    val refIndex: Int? = null
)

fun transformMatches(result: FuseResult, data: FormattedResult): FormattedResult {
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

fun transformScore(result: FuseResult, data: FormattedResult): FormattedResult {
    return data.copy(score = result.score)
}