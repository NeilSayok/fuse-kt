package com.fusekt.search.bitap

/**
 * Creates a character position mapping for efficient pattern matching.
 * 
 * This function builds a lookup table that maps each character in the pattern
 * to its bit positions, which is used by the Bitap algorithm for efficient
 * approximate string matching.
 *
 * @param pattern The search pattern to create alphabet for
 * @return Map from characters to their bit position masks
 */
fun createPatternAlphabet(pattern: String): Map<Char, Int> {
    val mask = mutableMapOf<Char, Int>()
    val len = pattern.length

    for (i in 0 until len) {
        val char = pattern[i]
        mask[char] = (mask[char] ?: 0) or (1 shl (len - i - 1))
    }

    return mask
}