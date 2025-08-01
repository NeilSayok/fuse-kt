package com.fusekt.search.bitap

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