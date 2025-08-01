package com.fusekt.core

import com.fusekt.helpers.get

data class MatchOptions(
    val includeMatches: Boolean = false,
    val findAllMatches: Boolean = false,
    val minMatchCharLength: Int = 1
)

data class BasicOptions(
    val isCaseSensitive: Boolean = false,
    val ignoreDiacritics: Boolean = false,
    val includeScore: Boolean = false,
    val keys: List<String> = emptyList(),
    val shouldSort: Boolean = true,
    val sortFn: (FuseResult, FuseResult) -> Int = { a, b ->
        when {
            a.score == b.score -> if (a.idx < b.idx) -1 else 1
            a.score < b.score -> -1
            else -> 1
        }
    }
)

data class FuzzyOptions(
    val location: Int = 0,
    val threshold: Double = 0.6,
    val distance: Int = 100
)

data class AdvancedOptions(
    val useExtendedSearch: Boolean = false,
    val getFn: (Any?, Any) -> Any? = ::get,
    val ignoreLocation: Boolean = false,
    val ignoreFieldNorm: Boolean = false,
    val fieldNormWeight: Double = 1.0
)

data class FuseOptions(
    val includeMatches: Boolean = false,
    val findAllMatches: Boolean = false,
    val minMatchCharLength: Int = 1,
    val isCaseSensitive: Boolean = false,
    val ignoreDiacritics: Boolean = false,
    val includeScore: Boolean = false,
    val keys: List<String> = emptyList(),
    val shouldSort: Boolean = true,
    val sortFn: (FuseResult, FuseResult) -> Int = { a, b ->
        when {
            a.score == b.score -> if (a.idx < b.idx) -1 else 1
            a.score < b.score -> -1
            else -> 1
        }
    },
    val location: Int = 0,
    val threshold: Double = 0.6,
    val distance: Int = 100,
    val useExtendedSearch: Boolean = false,
    val getFn: (Any?, Any) -> Any? = ::get,
    val ignoreLocation: Boolean = false,
    val ignoreFieldNorm: Boolean = false,
    val fieldNormWeight: Double = 1.0
)

data class FuseResult(
    val item: Any,
    val idx: Int,
    val score: Double = 0.0,
    val matches: List<FuseMatch> = emptyList()
)

data class FuseMatch(
    val score: Double,
    val key: String? = null,
    val value: String,
    val idx: Int? = null,
    val norm: Double? = null,
    val indices: List<IntRange> = emptyList()
)