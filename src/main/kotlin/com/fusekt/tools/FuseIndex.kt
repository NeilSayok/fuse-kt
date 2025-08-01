package com.fusekt.tools

import com.fusekt.core.FuseOptions
import com.fusekt.helpers.*
import kotlinx.serialization.Serializable

data class IndexRecord(
    val v: String? = null,
    val i: Int,
    val n: Double? = null,
    val `$`: Map<Int, Any>? = null
)

@Serializable
data class SubRecord(
    val v: String,
    val i: Int? = null,
    val n: Double
)

class FuseIndex(
    private val options: FuseOptions = FuseOptions()
) {
    private val norm = normGenerator(options.fieldNormWeight, 3)
    private val getFn = options.getFn
    var isCreated = false
        private set

    var docs: List<Any> = emptyList()
        private set
    var records: MutableList<IndexRecord> = mutableListOf()
        private set
    var keys: List<FuseKey> = emptyList()
        private set
    private var _keysMap: Map<String, Int> = emptyMap()

    fun setSources(docs: List<Any>) {
        this.docs = docs
    }

    fun setIndexRecords(records: MutableList<IndexRecord>) {
        this.records = records
    }

    fun setKeys(keys: List<FuseKey>) {
        this.keys = keys
        _keysMap = keys.mapIndexed { idx, key -> key.id to idx }.toMap()
    }

    fun create() {
        if (isCreated || docs.isEmpty()) {
            return
        }

        isCreated = true

        if (docs.isNotEmpty() && isString(docs[0])) {
            // List is Array<String>
            docs.forEachIndexed { docIndex, doc ->
                _addString(doc.toString(), docIndex)
            }
        } else {
            // List is Array<Object>
            docs.forEachIndexed { docIndex, doc ->
                _addObject(doc, docIndex)
            }
        }

        norm.clear()
    }

    fun add(doc: Any) {
        val idx = size()

        if (isString(doc)) {
            _addString(doc.toString(), idx)
        } else {
            _addObject(doc, idx)
        }
    }

    fun removeAt(idx: Int) {
        records.removeAt(idx)

        // Change ref index of every subsequent doc
        for (i in idx until size()) {
            records[i] = records[i].copy(i = records[i].i - 1)
        }
    }

    fun getValueForItemAtKeyId(item: Any, keyId: String): Any? {
        val keyIndex = _keysMap[keyId] ?: return null
        return when (item) {
            is Map<*, *> -> (item as Map<Int, Any>)[keyIndex]
            else -> null
        }
    }

    fun size(): Int = records.size

    private fun _addString(doc: String, docIndex: Int) {
        if (!isDefined(doc) || isBlank(doc)) {
            return
        }

        val record = IndexRecord(
            v = doc,
            i = docIndex,
            n = norm.get(doc)
        )

        records.add(record)
    }

    private fun _addObject(doc: Any, docIndex: Int) {
        val recordMap = mutableMapOf<Int, Any>()

        // Iterate over every key (i.e, path), and fetch the value at that key
        keys.forEachIndexed { keyIndex, key ->
            val value = if (key.getFn != null) {
                key.getFn.invoke(doc, key.path)
            } else {
                getFn(doc, key.path)
            }

            if (!isDefined(value)) {
                return@forEachIndexed
            }

            when {
                isArray(value) -> {
                    val subRecords = mutableListOf<SubRecord>()
                    val stack = mutableListOf<Pair<Int, Any?>>()
                    stack.add(-1 to value)

                    while (stack.isNotEmpty()) {
                        val (nestedArrIndex, currentValue) = stack.removeAt(stack.size - 1)

                        if (!isDefined(currentValue)) {
                            continue
                        }

                        when {
                            isString(currentValue) && !isBlank(currentValue as String) -> {
                                val subRecord = SubRecord(
                                    v = currentValue,
                                    i = nestedArrIndex,
                                    n = norm.get(currentValue)
                                )
                                subRecords.add(subRecord)
                            }
                            isArray(currentValue) -> {
                                when (currentValue) {
                                    is Array<*> -> currentValue.forEachIndexed { k, item ->
                                        stack.add(k to item)
                                    }
                                    is List<*> -> currentValue.forEachIndexed { k, item ->
                                        stack.add(k to item)
                                    }
                                }
                            }
                        }
                    }
                    recordMap[keyIndex] = subRecords
                }
                isString(value) && !isBlank(value as String) -> {
                    val subRecord = SubRecord(
                        v = value,
                        n = norm.get(value)
                    )
                    recordMap[keyIndex] = subRecord
                }
            }
        }

        val record = IndexRecord(
            i = docIndex,
            `$` = recordMap.toMap()
        )

        records.add(record)
    }

    fun toJSON(): Map<String, Any> {
        return mapOf(
            "keys" to keys,
            "records" to records
        )
    }
}

fun createIndex(
    keys: List<Any>,
    docs: List<Any>,
    options: FuseOptions = FuseOptions()
): FuseIndex {
    val myIndex = FuseIndex(options)
    myIndex.setKeys(keys.map { createKey(it) })
    myIndex.setSources(docs)
    myIndex.create()
    return myIndex
}

fun parseIndex(
    data: Map<String, Any>,
    options: FuseOptions = FuseOptions()
): FuseIndex {
    val keys = data["keys"] as List<FuseKey>
    val records = data["records"] as MutableList<IndexRecord>
    val myIndex = FuseIndex(options)
    myIndex.setKeys(keys)
    myIndex.setIndexRecords(records)
    return myIndex
}