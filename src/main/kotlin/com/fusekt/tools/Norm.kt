package com.fusekt.tools

import kotlin.math.pow
import kotlin.math.round

class NormGenerator(private val weight: Double = 1.0, private val mantissa: Int = 3) {
    private val cache = mutableMapOf<Int, Double>()
    private val m = 10.0.pow(mantissa)

    fun get(value: String): Double {
        val numTokens = value.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size

        if (cache.containsKey(numTokens)) {
            return cache[numTokens]!!
        }

        // Default function is 1/sqrt(x), weight makes that variable
        val norm = 1.0 / numTokens.toDouble().pow(0.5 * weight)

        // In place of toFixed(mantissa), for faster computation
        val n = round(norm * m) / m

        cache[numTokens] = n
        return n
    }

    fun clear() {
        cache.clear()
    }
}

fun normGenerator(weight: Double = 1.0, mantissa: Int = 3): NormGenerator {
    return NormGenerator(weight, mantissa)
}