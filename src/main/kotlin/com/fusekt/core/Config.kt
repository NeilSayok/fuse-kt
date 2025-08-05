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
    val sortFn: (FuseResult<*>, FuseResult<*>) -> Int = { a, b ->
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
    val keys: List<Any> = emptyList(),
    val shouldSort: Boolean = true,
    val sortFn: (FuseResult<*>, FuseResult<*>) -> Int = { a, b ->
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
) {
    companion object {
        /**
         * Creates FuseOptions with weighted keys from a map.
         * Example: 
         * FuseOptions.withWeightedKeys(
         *     mapOf(
         *         "title" to 0.8,
         *         "author" to 0.2
         *     )
         * )
         */
        fun withWeightedKeys(
            weightedKeys: Map<String, Double>,
            includeMatches: Boolean = false,
            findAllMatches: Boolean = false,
            minMatchCharLength: Int = 1,
            isCaseSensitive: Boolean = false,
            ignoreDiacritics: Boolean = false,
            includeScore: Boolean = false,
            shouldSort: Boolean = true,
            sortFn: (FuseResult<*>, FuseResult<*>) -> Int = { a, b ->
                when {
                    a.score == b.score -> if (a.idx < b.idx) -1 else 1
                    a.score < b.score -> -1
                    else -> 1
                }
            },
            location: Int = 0,
            threshold: Double = 0.6,
            distance: Int = 100,
            useExtendedSearch: Boolean = false,
            getFn: (Any?, Any) -> Any? = ::get,
            ignoreLocation: Boolean = false,
            ignoreFieldNorm: Boolean = false,
            fieldNormWeight: Double = 1.0
        ): FuseOptions {
            val keys = weightedKeys.map { (name, weight) ->
                mapOf(
                    "name" to name,
                    "weight" to weight
                )
            }
            
            return FuseOptions(
                includeMatches = includeMatches,
                findAllMatches = findAllMatches,
                minMatchCharLength = minMatchCharLength,
                isCaseSensitive = isCaseSensitive,
                ignoreDiacritics = ignoreDiacritics,
                includeScore = includeScore,
                keys = keys,
                shouldSort = shouldSort,
                sortFn = sortFn,
                location = location,
                threshold = threshold,
                distance = distance,
                useExtendedSearch = useExtendedSearch,
                getFn = getFn,
                ignoreLocation = ignoreLocation,
                ignoreFieldNorm = ignoreFieldNorm,
                fieldNormWeight = fieldNormWeight
            )
        }
    }
}

data class FuseResult<T>(
    val item: T,
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