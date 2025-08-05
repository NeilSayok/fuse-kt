package com.fusekt.tools

import com.fusekt.helpers.isArray
import com.fusekt.helpers.isString
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Represents a search key with its configuration and metadata.
 *
 * @property path The dot-notation path to the field (e.g., ["user", "name"])
 * @property id Unique identifier for this key
 * @property weight The importance weight of this key in search ranking
 * @property src The original source string used to create this key
 * @property getFn Optional custom function to extract values for this key
 */
data class FuseKey(
    val path: List<String>,
    val id: String,
    var weight: Double,
    val src: String,
    val getFn: ((Any?, Any) -> Any?)? = null
)

/**
 * Manages and normalizes search keys for Fuse.
 * 
 * KeyStore handles the conversion of various key formats (strings, arrays, weighted objects)
 * into normalized FuseKey objects with proper weight distribution.
 *
 * @param keys List of keys in various formats (strings, maps with weights, etc.)
 */
class KeyStore(keys: List<Any>) {
    private val _keys = mutableListOf<FuseKey>()
    private val _keyMap = mutableMapOf<String, FuseKey>()

    init {
        var totalWeight = 0.0

        keys.forEach { key ->
            val obj = createKey(key)
            _keys.add(obj)
            _keyMap[obj.id] = obj
            totalWeight += obj.weight
        }

        // Normalize weights so that their sum is equal to 1
        _keys.forEach { key ->
            key.weight /= totalWeight
        }
    }

    /**
     * Retrieves a FuseKey by its ID.
     *
     * @param keyId The unique identifier of the key
     * @return The FuseKey with the given ID, or null if not found
     */
    fun get(keyId: String): FuseKey? = _keyMap[keyId]

    /**
     * Returns all keys managed by this KeyStore.
     *
     * @return List of all FuseKey objects with normalized weights
     */
    fun keys(): List<FuseKey> = _keys

    /**
     * Converts the key store to a JSON-like string representation.
     *
     * @return String representation of all keys
     */
    fun toJSON(): String = _keys.toString()
}

/**
 * Creates a FuseKey from various input formats.
 * 
 * Supports:
 * - Simple strings: "title" -> FuseKey with path ["title"]
 * - Arrays: ["user", "name"] -> FuseKey with path ["user", "name"]
 * - Maps: {"name": "title", "weight": 0.8} -> Weighted FuseKey
 *
 * @param key The key specification in any supported format
 * @return A normalized FuseKey object
 * @throws IllegalArgumentException if the key format is invalid
 */
fun createKey(key: Any): FuseKey {
    var path: List<String>? = null
    var id: String? = null
    var src: String = key.toString()
    var weight = 1.0
    var getFn: ((Any?, Any) -> Any?)? = null

    when {
        isString(key) || isArray(key) -> {
            src = key.toString()
            path = createKeyPath(key)
            id = createKeyId(key)
        }
        key is Map<*, *> -> {
            val keyMap = key as Map<String, Any>
            if (!keyMap.containsKey("name")) {
                throw IllegalArgumentException("Missing key property 'name'")
            }

            val name = keyMap["name"]!!
            src = name.toString()

            if (keyMap.containsKey("weight")) {
                weight = (keyMap["weight"] as Number).toDouble()
                if (weight <= 0) {
                    throw IllegalArgumentException("Invalid key weight value for '$name'")
                }
            }

            path = createKeyPath(name)
            id = createKeyId(name)
            getFn = keyMap["getFn"] as? ((Any?, Any) -> Any?)
        }
        else -> {
            path = createKeyPath(key)
            id = createKeyId(key)
        }
    }

    return FuseKey(
        path = path!!,
        id = id!!,
        weight = weight,
        src = src,
        getFn = getFn
    )
}

/**
 * Creates a path array from various key formats.
 *
 * @param key The key to convert to a path
 * @return List of strings representing the path to the field
 */
fun createKeyPath(key: Any): List<String> {
    return when {
        isArray(key) -> (key as List<*>).map { it.toString() }
        else -> key.toString().split(".")
    }
}

/**
 * Creates a unique identifier string from a key.
 *
 * @param key The key to create an ID for
 * @return A string identifier for the key
 */
fun createKeyId(key: Any): String {
    return when {
        isArray(key) -> (key as List<*>).joinToString(".")
        else -> key.toString()
    }
}