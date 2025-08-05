package com.fusekt.search.bitap

fun convertMaskToIndices(
    matchMask: List<Int> = emptyList(),
    minMatchCharLength: Int = 1
): List<IntRange> {
    val indices = mutableListOf<IntRange>()
    var start = -1
    var end = -1
    var i = 0

    val len = matchMask.size
    while (i < len) {
        val match = matchMask[i]
        if (match != 0 && start == -1) {
            start = i
        } else if (match == 0 && start != -1) {
            end = i - 1
            if (end - start + 1 >= minMatchCharLength) {
                indices.add(start..end)
            }
            start = -1
        }
        i++
    }

    if (i > 0 && matchMask[i - 1] != 0 && i - start >= minMatchCharLength) {
        indices.add(start until i)
    }

    return indices
}