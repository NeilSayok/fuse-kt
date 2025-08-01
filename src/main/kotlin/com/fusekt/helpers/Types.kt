package com.fusekt.helpers

fun isArray(value: Any?): Boolean = value is Array<*> || value is List<*>

fun baseToString(value: Any?): String {
    if (value is String) return value
    val result = value.toString()
    return if (result == "0" && value is Double && 1.0 / value == Double.NEGATIVE_INFINITY) "-0" else result
}

fun valueToString(value: Any?): String = value?.let { baseToString(it) } ?: ""

fun isString(value: Any?): Boolean = value is String

fun isNumber(value: Any?): Boolean = value is Number

fun isBoolean(value: Any?): Boolean = value is Boolean

fun isObject(value: Any?): Boolean = value != null && !isString(value) && !isNumber(value) && !isBoolean(value)

fun isDefined(value: Any?): Boolean = value != null

fun isBlank(value: String): Boolean = value.trim().isEmpty()