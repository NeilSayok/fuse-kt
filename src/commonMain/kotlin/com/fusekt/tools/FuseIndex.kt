package com.fusekt.tools

import com.fusekt.core.FuseOptions
import com.fusekt.helpers.*
import kotlinx.serialization.Serializable

/**
 * Represents a single record in the search index.
 *
 * @property v The indexed text value
 * @property i The document index in the original collection
 * @property n The normalized score for field length
 * @property item Map of key indices to their extracted values
 */
data class IndexRecord(
    val v: String? = null,
    val i: Int,
    val n: Double? = null,
    val item: Map<Int, Any>? = null
)

/**
 * Represents a sub-record within an indexed field (for array values).
 *
 * @property v The text value
 * @property i The index within the array
 * @property n The normalized score
 */
@Serializable
data class SubRecord(
    val v: String,
    val i: Int? = null,
    val n: Double
)

/**
 * Pre-built search index for efficient fuzzy searching.
 * 
 * FuseIndex processes and stores document data in an optimized format
 * for fast fuzzy search operations. It handles field extraction,
 * normalization, and weight calculations.
 *
 * @param T The type of documents being indexed
 * @param options Configuration options for indexing behavior
 */
class FuseIndex<T>(
    private val options: FuseOptions = FuseOptions()
) {
    private val norm = normGenerator(options.fieldNormWeight, 3)
    private val getFn = options.getFn
    var isCreated = false
        private set

    var docs: List<T> = emptyList()
        private set
    var records: MutableList<IndexRecord> = mutableListOf()
        private set
    var keys: List<FuseKey> = emptyList()
        private set
    private var _keysMap: Map<String, Int> = emptyMap()

    /**
     * Sets the source documents for this index.
     *
     * @param docs List of documents to index
     */
    fun setSources(docs: List<T>) {
        this.docs = docs
    }

    /**
     * Sets the index records directly (used when loading from serialized data).
     *
     * @param records Pre-built index records
     */
    fun setIndexRecords(records: MutableList<IndexRecord>) {
        this.records = records
    }

    /**
     * Sets the search keys for this index.
     *
     * @param keys List of FuseKey objects defining which fields to index
     */
    fun setKeys(keys: List<FuseKey>) {
        this.keys = keys
        _keysMap = keys.mapIndexed { idx, key -> key.id to idx }.toMap()
    }

    /**
     * Builds the search index from the configured sources and keys.
     * This method processes all documents and extracts searchable text
     * according to the configured keys and options.
     */
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

    /**
     * Adds a new document to the existing index.
     *
     * @param doc The document to add and index
     */
    fun add(doc: T) {
        val idx = size()

        if (isString(doc)) {
            _addString(doc.toString(), idx)
        } else {
            _addObject(doc, idx)
        }
    }

    /**
     * Removes a document from the index at the specified position.
     *
     * @param idx The index of the document to remove
     */
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

    private fun _addObject(doc: T, docIndex: Int) {
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
            item = recordMap.toMap()
        )

        records.add(record)
    }

    /**
     * Serializes the index to a map that can be converted to JSON.
     * Useful for saving and loading pre-built indices.
     *
     * @return Map containing the keys and records data
     */
    fun toJSON(): Map<String, Any> {
        return mapOf(
            "keys" to keys,
            "records" to records
        )
    }
}

/**
 * Creates a new search index from documents and key specifications.
 *
 * @param T The type of documents being indexed
 * @param keys List of key specifications (strings, arrays, or weighted maps)
 * @param docs List of documents to index
 * @param options Configuration options for indexing
 * @return A fully built FuseIndex ready for searching
 */
fun <T> createIndex(
    keys: List<Any>,
    docs: List<T>,
    options: FuseOptions = FuseOptions()
): FuseIndex<T> {
    val myIndex = FuseIndex<T>(options)
    myIndex.setKeys(keys.map { createKey(it) })
    myIndex.setSources(docs)
    myIndex.create()
    return myIndex
}

/**
 * Recreates a FuseIndex from serialized data.
 *
 * @param T The type of documents being indexed
 * @param data Map containing serialized index data (from toJSON())
 * @param options Configuration options
 * @return A FuseIndex reconstructed from the serialized data
 */
fun <T> parseIndex(
    data: Map<String, Any>,
    options: FuseOptions = FuseOptions()
): FuseIndex<T> {
    val keys = data["keys"] as List<FuseKey>
    val records = data["records"] as MutableList<IndexRecord>
    val myIndex = FuseIndex<T>(options)
    myIndex.setKeys(keys)
    myIndex.setIndexRecords(records)
    return myIndex
}