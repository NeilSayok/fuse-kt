package com.fusekt.search.bitap

import com.fusekt.core.FuseOptions
import com.fusekt.helpers.stripDiacritics
import kotlin.math.*

/**
 * Result of a search operation.
 *
 * @property isMatch Whether a match was found
 * @property score The relevance score (lower is better)
 * @property indices List of character ranges that matched in the text
 */
data class SearchResult(
    val isMatch: Boolean,
    val score: Double,
    val indices: List<IntRange> = emptyList()
)

/**
 * Represents a chunk of the search pattern for bitap algorithm processing.
 *
 * @property pattern The pattern string for this chunk
 * @property alphabet Character position mapping for efficient matching
 * @property startIndex Starting position of this chunk in the original pattern
 */
data class PatternChunk(
    val pattern: String,
    val alphabet: Map<Char, Int>,
    val startIndex: Int
)

/**
 * Bitap fuzzy string searching algorithm implementation.
 * 
 * This class implements the Bitap (Baeza-Yates-Gonnet) algorithm for approximate
 * string matching. It supports fuzzy matching with configurable parameters for
 * location bias, distance tolerance, and match thresholds.
 *
 * The algorithm is particularly well-suited for finding approximate matches
 * in text with support for insertions, deletions, and substitutions.
 *
 * @param pattern The search pattern to find
 * @param options Configuration options for the search behavior
 */
class BitapSearch(
    pattern: String,
    options: FuseOptions = FuseOptions()
) {
    private val options = options
    private val pattern: String
    private val chunks = mutableListOf<PatternChunk>()

    init {
        var processedPattern = if (options.isCaseSensitive) pattern else pattern.lowercase()
        processedPattern = if (options.ignoreDiacritics) stripDiacritics(processedPattern) else processedPattern
        this.pattern = processedPattern

        if (this.pattern.isNotEmpty()) {
            val addChunk = { pattern: String, startIndex: Int ->
                chunks.add(PatternChunk(
                    pattern = pattern,
                    alphabet = createPatternAlphabet(pattern),
                    startIndex = startIndex
                ))
            }

            val len = this.pattern.length

            if (len > MAX_BITS) {
                var i = 0
                val remainder = len % MAX_BITS
                val end = len - remainder

                while (i < end) {
                    addChunk(this.pattern.substring(i, i + MAX_BITS), i)
                    i += MAX_BITS
                }

                if (remainder > 0) {
                    val startIndex = len - MAX_BITS
                    addChunk(this.pattern.substring(startIndex), startIndex)
                }
            } else {
                addChunk(this.pattern, 0)
            }
        }
    }

    /**
     * Performs a fuzzy search for the pattern within the given text.
     *
     * @param text The text to search within
     * @return SearchResult indicating whether a match was found, its score, and match indices
     */
    fun searchIn(text: String): SearchResult {
        var processedText = if (options.isCaseSensitive) text else text.lowercase()
        processedText = if (options.ignoreDiacritics) stripDiacritics(processedText) else processedText

        // Exact match
        if (pattern == processedText) {
            val result = SearchResult(
                isMatch = true,
                score = 0.0,
                indices = if (options.includeMatches) listOf(0 until text.length) else emptyList()
            )
            return result
        }

        // Use bitap algorithm
        val allIndices = mutableListOf<IntRange>()
        var totalScore = 0.0
        var hasMatches = false

        chunks.forEach { chunk ->
            val result = search(
                text = processedText,
                pattern = chunk.pattern,
                patternAlphabet = chunk.alphabet,
                location = options.location + chunk.startIndex,
                distance = options.distance,
                threshold = options.threshold,
                findAllMatches = options.findAllMatches,
                minMatchCharLength = options.minMatchCharLength,
                includeMatches = options.includeMatches,
                ignoreLocation = options.ignoreLocation
            )

            if (result.isMatch) {
                hasMatches = true
            }

            totalScore += result.score

            if (result.isMatch && result.indices.isNotEmpty()) {
                allIndices.addAll(result.indices)
            }
        }

        return SearchResult(
            isMatch = hasMatches,
            score = if (hasMatches) totalScore / chunks.size else 1.0,
            indices = if (hasMatches && options.includeMatches) allIndices else emptyList()
        )
    }

    private fun search(
        text: String,
        pattern: String,
        patternAlphabet: Map<Char, Int>,
        location: Int = 0,
        distance: Int = 100,
        threshold: Double = 0.6,
        findAllMatches: Boolean = false,
        minMatchCharLength: Int = 1,
        includeMatches: Boolean = false,
        ignoreLocation: Boolean = false
    ): SearchResult {
        if (pattern.length > MAX_BITS) {
            throw IllegalArgumentException("Pattern length ($pattern.length) exceeds maximum ($MAX_BITS)")
        }

        val patternLen = pattern.length
        val textLen = text.length
        val expectedLocation = maxOf(0, minOf(location, textLen))
        var currentThreshold = threshold
        var bestLocation = expectedLocation

        val computeMatches = minMatchCharLength > 1 || includeMatches
        val matchMask = if (computeMatches) MutableList(textLen) { 0 } else mutableListOf<Int>()

        // Get all exact matches for speed up
        var index = text.indexOf(pattern, bestLocation)
        while (index > -1) {
            val score = computeScore(
                pattern = pattern,
                currentLocation = index,
                expectedLocation = expectedLocation,
                distance = distance,
                ignoreLocation = ignoreLocation
            )

            currentThreshold = minOf(score, currentThreshold)
            bestLocation = index + patternLen

            if (computeMatches) {
                for (i in 0 until patternLen) {
                    matchMask[index + i] = 1
                }
            }

            index = text.indexOf(pattern, bestLocation)
        }

        bestLocation = -1

        var lastBitArr = mutableListOf<Int>()
        var finalScore = 1.0
        var binMax = patternLen + textLen

        val mask = 1 shl (patternLen - 1)

        for (i in 0 until patternLen) {
            var binMin = 0
            var binMid = binMax

            while (binMin < binMid) {
                val score = computeScore(
                    pattern = pattern,
                    errors = i,
                    currentLocation = expectedLocation + binMid,
                    expectedLocation = expectedLocation,
                    distance = distance,
                    ignoreLocation = ignoreLocation
                )

                if (score <= currentThreshold) {
                    binMin = binMid
                } else {
                    binMax = binMid
                }

                binMid = (binMax - binMin) / 2 + binMin
            }

            binMax = binMid

            val start = maxOf(1, expectedLocation - binMid + 1)
            val finish = if (findAllMatches) textLen else minOf(expectedLocation + binMid, textLen) + patternLen

            val bitArr = MutableList(finish + 2) { 0 }
            bitArr[finish + 1] = (1 shl i) - 1

            for (j in finish downTo start) {
                val currentLocation = j - 1
                val charMatch = if (currentLocation >= 0 && currentLocation < textLen) {
                    patternAlphabet[text[currentLocation]] ?: 0
                } else {
                    0
                }

                if (computeMatches && currentLocation >= 0 && currentLocation < matchMask.size) {
                    matchMask[currentLocation] = if (charMatch != 0) 1 else 0
                }

                // First pass: exact match
                bitArr[j] = ((bitArr[j + 1] shl 1) or 1) and charMatch

                // Subsequent passes: fuzzy match
                if (i > 0) {
                    bitArr[j] = bitArr[j] or 
                        (((lastBitArr.getOrElse(j + 1) { 0 } or lastBitArr.getOrElse(j) { 0 }) shl 1) or 1 or lastBitArr.getOrElse(j + 1) { 0 })
                }

                if ((bitArr[j] and mask) != 0) {
                    finalScore = computeScore(
                        pattern = pattern,
                        errors = i,
                        currentLocation = currentLocation,
                        expectedLocation = expectedLocation,
                        distance = distance,
                        ignoreLocation = ignoreLocation
                    )

                    if (finalScore <= currentThreshold) {
                        currentThreshold = finalScore
                        bestLocation = currentLocation

                        if (bestLocation <= expectedLocation) {
                            break
                        }

                        // When passing bestLocation, don't exceed our current distance from expectedLocation
                        val newStart = maxOf(1, 2 * expectedLocation - bestLocation)
                        if (newStart > start) break
                    }
                }
            }

            val score = computeScore(
                pattern = pattern,
                errors = i + 1,
                currentLocation = expectedLocation,
                expectedLocation = expectedLocation,
                distance = distance,
                ignoreLocation = ignoreLocation
            )

            if (score > currentThreshold) {
                break
            }

            lastBitArr = bitArr.toMutableList()
        }

        val result = SearchResult(
            isMatch = bestLocation >= 0,
            score = maxOf(0.001, finalScore)
        )

        return if (computeMatches) {
            val indices = convertMaskToIndices(matchMask, minMatchCharLength)
            if (indices.isEmpty()) {
                result.copy(isMatch = false)
            } else if (includeMatches) {
                result.copy(indices = indices)
            } else {
                result
            }
        } else {
            result
        }
    }
}