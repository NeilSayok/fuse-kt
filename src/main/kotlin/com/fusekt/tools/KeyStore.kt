package com.fusekt.tools

import com.fusekt.helpers.isArray
import com.fusekt.helpers.isString
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class FuseKey(
    val path: List<String>,
    val id: String,
    var weight: Double,
    val src: String,
    val getFn: ((Any?, Any) -> Any?)? = null
)

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

    fun get(keyId: String): FuseKey? = _keyMap[keyId]

    fun keys(): List<FuseKey> = _keys

    fun toJSON(): String = _keys.toString()
}

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

fun createKeyPath(key: Any): List<String> {
    return when {
        isArray(key) -> (key as List<*>).map { it.toString() }
        else -> key.toString().split(".")
    }
}

fun createKeyId(key: Any): String {
    return when {
        isArray(key) -> (key as List<*>).joinToString(".")
        else -> key.toString()
    }
}