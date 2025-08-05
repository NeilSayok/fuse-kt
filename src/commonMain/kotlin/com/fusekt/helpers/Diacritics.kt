package com.fusekt.helpers

/**
 * Removes diacritics (accents) from a string for normalized searching.
 * 
 * This function normalizes Unicode text by removing combining diacritical marks,
 * allowing for accent-insensitive fuzzy searching. The implementation varies
 * by platform to use the most appropriate text normalization available.
 *
 * @param str The string to remove diacritics from
 * @return The string with diacritics removed
 *
 * @sample
 * ```kotlin
 * stripDiacritics("café") // Returns "cafe"
 * stripDiacritics("naïve") // Returns "naive"
 * stripDiacritics("résumé") // Returns "resume"
 * ```
 */
expect fun stripDiacritics(str: String): String