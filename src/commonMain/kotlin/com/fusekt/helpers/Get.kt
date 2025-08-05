package com.fusekt.helpers

/**
 * Extracts values from an object using a dot-notation path.
 * 
 * This function can traverse nested objects and extract values from various data structures
 * including Maps and plain objects. It supports both string paths ("user.name") and
 * array paths (["user", "name"]).
 *
 * @param obj The object to extract values from
 * @param path The path to the desired value (string with dots or array of strings)
 * @return The extracted value(s), or null if not found
 *
 * @sample
 * ```kotlin
 * val user = mapOf(
 *     "name" to "John",
 *     "address" to mapOf(
 *         "city" to "New York"
 *     )
 * )
 * val city = get(user, "address.city") // Returns "New York"
 * ```
 */
fun get(obj: Any?, path: Any): Any? {
    val list = mutableListOf<Any>()
    var isArray = false

    fun deepGet(obj: Any?, pathList: List<String>, index: Int) {
        if (!isDefined(obj) || index >= pathList.size) {
            if (index >= pathList.size && obj != null) {
                list.add(obj)
            }
            return
        }

        val key = pathList[index]
        val value = when (obj) {
            is Map<*, *> -> obj[key]
            else -> {
                null
            }
        }

        if (!isDefined(value)) return

        if (index == pathList.size - 1 && (isString(value) || isNumber(value) || isBoolean(value))) {
            list.add(valueToString(value))
        } else if (isArray(value)) {
            isArray = true
            when (value) {
                is Array<*> -> value.forEachIndexed { _, item -> deepGet(item, pathList, index + 1) }
                is List<*> -> value.forEach { item -> deepGet(item, pathList, index + 1) }
            }
        } else if (pathList.isNotEmpty()) {
            deepGet(value, pathList, index + 1)
        }
    }

    val pathList = when (path) {
        is String -> path.split(".")
        is List<*> -> path.map { it.toString() }
        else -> listOf(path.toString())
    }

    deepGet(obj, pathList, 0)

    return if (isArray) list else list.firstOrNull()
}