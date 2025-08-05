package com.fusekt.search.bitap

/**
 * Computes the relevance score for a fuzzy match.
 * 
 * The score combines accuracy (based on edit distance) with location proximity
 * to provide a comprehensive relevance measure. Lower scores indicate better matches.
 *
 * @param pattern The search pattern
 * @param errors Number of errors (insertions, deletions, substitutions) in the match
 * @param currentLocation The actual location where the match was found
 * @param expectedLocation The expected location for the match
 * @param distance Maximum distance from expected location before score penalty
 * @param ignoreLocation Whether to ignore location-based scoring
 * @return Relevance score (lower is better, 0.0 is perfect match)
 */
fun computeScore(
    pattern: String,
    errors: Int = 0,
    currentLocation: Int = 0,
    expectedLocation: Int = 0,
    distance: Int = 100,
    ignoreLocation: Boolean = false
): Double {
    val accuracy = errors.toDouble() / pattern.length

    if (ignoreLocation) {
        return accuracy
    }

    val proximity = kotlin.math.abs(expectedLocation - currentLocation)

    if (distance == 0) {
        return if (proximity != 0) 1.0 else accuracy
    }

    return accuracy + proximity.toDouble() / distance
}