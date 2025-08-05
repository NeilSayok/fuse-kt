package com.fusekt.core

import com.fusekt.helpers.get

/**
 * Configuration options for controlling match behavior in fuzzy search.
 *
 * @property includeMatches Whether to include information about which parts of the text matched the search pattern
 * @property findAllMatches Whether to find all matches in a string, not just the first one
 * @property minMatchCharLength Minimum number of characters that must match for a result to be considered valid
 */
data class MatchOptions(
    val includeMatches: Boolean = false,
    val findAllMatches: Boolean = false,
    val minMatchCharLength: Int = 1
)

/**
 * Basic configuration options for fuzzy search.
 *
 * @property isCaseSensitive Whether the search should be case sensitive
 * @property ignoreDiacritics Whether to ignore diacritics (accents) in the search
 * @property includeScore Whether to include the relevance score in search results
 * @property keys List of field names to search in
 * @property shouldSort Whether to sort results by relevance score
 * @property sortFn Custom sorting function for search results
 */
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

/**
 * Configuration options for fuzzy matching algorithm.
 *
 * @property location Approximately where in the text is the pattern expected to be found (0 = beginning)
 * @property threshold At what point does the match algorithm give up. A threshold of 0.0 requires a perfect match, 1.0 would match anything
 * @property distance Determines how close the match must be to the fuzzy location (specified by location)
 */
data class FuzzyOptions(
    val location: Int = 0,
    val threshold: Double = 0.6,
    val distance: Int = 100
)

/**
 * Advanced configuration options for fuzzy search.
 *
 * @property useExtendedSearch Whether to use extended search syntax
 * @property getFn Function to extract values from objects
 * @property ignoreLocation Whether to ignore location-based scoring
 * @property ignoreFieldNorm Whether to ignore field length normalization
 * @property fieldNormWeight The weight to give to field length normalization
 */
data class AdvancedOptions(
    val useExtendedSearch: Boolean = false,
    val getFn: (Any?, Any) -> Any? = ::get,
    val ignoreLocation: Boolean = false,
    val ignoreFieldNorm: Boolean = false,
    val fieldNormWeight: Double = 1.0
)

/**
 * Complete configuration options for Fuse fuzzy search.
 * Combines all option categories into a single configuration object.
 *
 * @property includeMatches Whether to include information about which parts of the text matched
 * @property findAllMatches Whether to find all matches in a string, not just the first one
 * @property minMatchCharLength Minimum number of characters that must match
 * @property isCaseSensitive Whether the search should be case sensitive
 * @property ignoreDiacritics Whether to ignore diacritics (accents)
 * @property includeScore Whether to include the relevance score in results
 * @property keys List of field names or weighted key configurations to search in
 * @property shouldSort Whether to sort results by relevance score
 * @property sortFn Custom sorting function for search results
 * @property location Approximately where in the text the pattern is expected
 * @property threshold Match threshold (0.0 = perfect match, 1.0 = match anything)
 * @property distance How close the match must be to the expected location
 * @property useExtendedSearch Whether to use extended search syntax
 * @property getFn Function to extract values from objects
 * @property ignoreLocation Whether to ignore location-based scoring
 * @property ignoreFieldNorm Whether to ignore field length normalization
 * @property fieldNormWeight Weight for field length normalization
 */
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
         * 
         * This is a convenience method for creating search configurations where different
         * fields have different importance weights. Higher weights make fields more important
         * in the search ranking.
         *
         * @param weightedKeys Map of field names to their weights (0.0 to 1.0)
         * @param includeMatches Whether to include match information in results
         * @param findAllMatches Whether to find all matches, not just the first
         * @param minMatchCharLength Minimum character length for matches
         * @param isCaseSensitive Whether search is case sensitive
         * @param ignoreDiacritics Whether to ignore accents and diacritics
         * @param includeScore Whether to include relevance scores
         * @param shouldSort Whether to sort results by score
         * @param sortFn Custom sorting function
         * @param location Expected location of pattern in text
         * @param threshold Match threshold (0.0-1.0)
         * @param distance Distance from expected location
         * @param useExtendedSearch Whether to use extended search syntax
         * @param getFn Custom value extraction function
         * @param ignoreLocation Whether to ignore location scoring
         * @param ignoreFieldNorm Whether to ignore field normalization
         * @param fieldNormWeight Weight for field normalization
         * @return FuseOptions configured with weighted keys
         *
         * @sample
         * ```kotlin
         * val options = FuseOptions.withWeightedKeys(
         *     mapOf(
         *         "title" to 0.8,    // Title is very important
         *         "author" to 0.2    // Author is less important
         *     ),
         *     threshold = 0.3
         * )
         * ```
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

/**
 * Represents a single search result from Fuse.
 *
 * @param T The type of the items being searched
 * @property item The original item that matched the search
 * @property idx The index of the item in the original dataset
 * @property score The relevance score (lower is better, 0.0 is perfect match)
 * @property matches List of detailed match information for each field
 */
data class FuseResult<T>(
    val item: T,
    val idx: Int,
    val score: Double = 0.0,
    val matches: List<FuseMatch> = emptyList()
)

/**
 * Detailed information about a match within a search result.
 *
 * @property score The match score for this specific field
 * @property key The name of the field that matched
 * @property value The actual text that was matched
 * @property idx The index within arrays (if the field contains an array)
 * @property norm The normalized score
 * @property indices The character ranges that matched within the text
 */
data class FuseMatch(
    val score: Double,
    val key: String? = null,
    val value: String,
    val idx: Int? = null,
    val norm: Double? = null,
    val indices: List<IntRange> = emptyList()
)