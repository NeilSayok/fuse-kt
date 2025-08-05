package com.fusekt.helpers

/**
 * Checks if a value is an array or list.
 *
 * @param value The value to check
 * @return true if the value is an Array or List, false otherwise
 */
fun isArray(value: Any?): Boolean = value is Array<*> || value is List<*>

/**
 * Converts a value to a string with special handling for edge cases.
 *
 * @param value The value to convert
 * @return String representation of the value
 */
fun baseToString(value: Any?): String {
    if (value is String) return value
    val result = value.toString()
    return if (result == "0" && value is Double && 1.0 / value == Double.NEGATIVE_INFINITY) "-0" else result
}

/**
 * Safely converts a value to a string, returning empty string for null.
 *
 * @param value The value to convert
 * @return String representation or empty string if null
 */
fun valueToString(value: Any?): String = value?.let { baseToString(it) } ?: ""

/**
 * Checks if a value is a string.
 *
 * @param value The value to check
 * @return true if the value is a String, false otherwise
 */
fun isString(value: Any?): Boolean = value is String

/**
 * Checks if a value is a number.
 *
 * @param value The value to check
 * @return true if the value is a Number, false otherwise
 */
fun isNumber(value: Any?): Boolean = value is Number

/**
 * Checks if a value is a boolean.
 *
 * @param value The value to check
 * @return true if the value is a Boolean, false otherwise
 */
fun isBoolean(value: Any?): Boolean = value is Boolean

/**
 * Checks if a value is an object (not a primitive type).
 *
 * @param value The value to check
 * @return true if the value is an object type, false for primitives or null
 */
fun isObject(value: Any?): Boolean = value != null && !isString(value) && !isNumber(value) && !isBoolean(value)

/**
 * Checks if a value is defined (not null).
 *
 * @param value The value to check
 * @return true if the value is not null, false otherwise
 */
fun isDefined(value: Any?): Boolean = value != null

/**
 * Checks if a string is blank (empty or only whitespace).
 *
 * @param value The string to check
 * @return true if the string is blank, false otherwise
 */
fun isBlank(value: String): Boolean = value.trim().isEmpty()