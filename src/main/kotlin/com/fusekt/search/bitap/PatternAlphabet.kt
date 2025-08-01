package com.fusekt.search.bitap

fun createPatternAlphabet(pattern: String): Map<Char, Int> {
    val mask = mutableMapOf<Char, Int>()
    val len = pattern.length

    for (i in 0 until len) {
        val char = pattern[i]
        mask[char] = (mask[char] ?: 0) or (1 shl (len - i - 1))
    }

    return mask
}