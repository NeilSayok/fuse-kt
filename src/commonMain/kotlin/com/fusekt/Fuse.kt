package com.fusekt

import com.fusekt.core.*
import com.fusekt.helpers.*
import com.fusekt.search.bitap.BitapSearch
import com.fusekt.tools.*
import com.fusekt.transform.FormattedResult

/**
 * Fuse is a lightweight fuzzy-search library for Kotlin Multiplatform.
 * 
 * It provides powerful and flexible fuzzy search capabilities with support for:
 * - Weighted field searching
 * - Customizable match thresholds
 * - Match highlighting
 * - Multiple search algorithms
 * - Cross-platform compatibility (JVM, JS, Native)
 *
 * @param T The type of items being searched
 * @param docs The list of documents/objects to search through
 * @param options Configuration options for the search behavior
 * @param index Optional pre-built index for faster repeated searches
 *
 * @sample
 * ```kotlin
 * data class Book(val title: String, val author: String)
 * 
 * val books = listOf(
 *     Book("The Great Gatsby", "F. Scott Fitzgerald"),
 *     Book("To Kill a Mockingbird", "Harper Lee")
 * )
 * 
 * val options = FuseOptions.withWeightedKeys(
 *     mapOf("title" to 0.8, "author" to 0.2)
 * )
 * 
 * val fuse = Fuse<Book>(books, options)
 * val results = fuse.search("gatsby")
 * ```
 */
class Fuse<T>(
    docs: List<T>,
    options: FuseOptions = FuseOptions(),
    index: FuseIndex<T>? = null
) {
    private val options: FuseOptions = options
    private val _keyStore: KeyStore = KeyStore(options.keys)
    private var _docs: MutableList<T> = docs.toMutableList()
    private var _myIndex: FuseIndex<T> = index ?: createIndex(options.keys, docs, options)

    companion object {
        /** Current version of the Fuse library */
        const val version = "1.0.0"
        
        /**
         * Creates a search index for the given documents and keys.
         * 
         * Pre-building an index can significantly improve search performance
         * when performing multiple searches on the same dataset.
         *
         * @param keys List of field names or weighted key configurations to index
         * @param docs List of documents to index
         * @param options Configuration options for indexing
         * @return A FuseIndex that can be reused for multiple searches
         */
        fun <T> createIndex(keys: List<Any>, docs: List<T>, options: FuseOptions = FuseOptions()): FuseIndex<T> {
            return com.fusekt.tools.createIndex(keys, docs, options)
        }
        
        /**
         * Parses a previously serialized index back into a FuseIndex object.
         *
         * @param data The serialized index data
         * @param options Configuration options
         * @return A FuseIndex reconstructed from the data
         */
        fun <T> parseIndex(data: Map<String, Any>, options: FuseOptions = FuseOptions()): FuseIndex<T> {
            return com.fusekt.tools.parseIndex<T>(data, options)
        }
    }

    init {
        if (options.useExtendedSearch) {
            throw UnsupportedOperationException("Extended search is not yet implemented")
        }

        setCollection(docs, index)
    }

    private fun setCollection(docs: List<T>, index: FuseIndex<T>? = null) {
        _docs = docs.toMutableList()

        if (index != null && index !is FuseIndex<*>) {
            throw IllegalArgumentException("Incorrect index type")
        }

        _myIndex = index ?: com.fusekt.tools.createIndex(
            keys = options.keys,
            docs = _docs,
            options = options
        )
    }

    /**
     * Adds a new document to the search index.
     *
     * @param doc The document to add to the searchable collection
     */
    fun add(doc: T) {
        if (!isDefined(doc)) {
            return
        }

        _docs.add(doc)
        _myIndex.add(doc)
    }

    /**
     * Removes documents from the search index based on a predicate function.
     *
     * @param predicate Function that returns true for documents that should be removed
     * @return List of documents that were removed
     */
    fun remove(predicate: (T, Int) -> Boolean = { _, _ -> false }): List<T> {
        val results = mutableListOf<T>()

        var i = 0
        while (i < _docs.size) {
            val doc = _docs[i]
            if (predicate(doc, i)) {
                removeAt(i)
                results.add(doc)
            } else {
                i++
            }
        }

        return results
    }

    /**
     * Removes a document at the specified index.
     *
     * @param idx The index of the document to remove
     */
    fun removeAt(idx: Int) {
        _docs.removeAt(idx)
        _myIndex.removeAt(idx)
    }

    /**
     * Returns the current search index.
     *
     * @return The FuseIndex being used for searches
     */
    fun getIndex(): FuseIndex<T> = _myIndex

    /**
     * Performs a fuzzy search on the indexed documents.
     *
     * @param query The search query string
     * @param limit Maximum number of results to return (-1 for no limit)
     * @return List of search results ordered by relevance
     *
     * @sample
     * ```kotlin
     * val results = fuse.search("great gatsby", limit = 5)
     * results.forEach { result ->
     *     println("Found: ${result.item} (score: ${result.score})")
     * }
     * ```
     */
    fun search(query: Any, limit: Int = -1): List<FormattedResult<T>> {
        val results = when {
            isString(query) -> {
                if (_docs.isNotEmpty() && isString(_docs[0])) {
                    _searchStringList(query as String)
                } else {
                    _searchObjectList(query as String)
                }
            }
            else -> throw UnsupportedOperationException("Logical search not yet implemented")
        }

        computeScore<T>(results.toMutableList(), options.ignoreFieldNorm)

        val sortedResults = if (options.shouldSort) {
            results.sortedWith { a, b -> options.sortFn(a, b) }
        } else {
            results
        }

        val limitedResults = if (isNumber(limit) && limit > -1) {
            sortedResults.take(limit)
        } else {
            sortedResults
        }

        return format<T>(
            results = limitedResults,
            docs = _docs,
            includeMatches = options.includeMatches,
            includeScore = options.includeScore
        )
    }

    private fun _searchStringList(query: String): List<FuseResult<T>> {
        val searcher = BitapSearch(query, options)
        val records = _myIndex.records
        val results = mutableListOf<FuseResult<T>>()

        records.forEach { record ->
            val text = record.v
            if (!isDefined(text)) {
                return@forEach
            }

            val searchResult = searcher.searchIn(text!!)

            if (searchResult.isMatch) {
                results.add(
                    FuseResult<T>(
                        item = text as T,
                        idx = record.i,
                        matches = listOf(
                            FuseMatch(
                                score = searchResult.score,
                                value = text,
                                norm = record.n,
                                indices = searchResult.indices
                            )
                        )
                    )
                )
            }
        }

        return results
    }

    private fun _searchObjectList(query: String): List<FuseResult<T>> {
        val searcher = BitapSearch(query, options)
        val keys = _myIndex.keys
        val records = _myIndex.records
        val results = mutableListOf<FuseResult<T>>()

        records.forEach { record ->
            val item = record.item
            if (!isDefined(item)) {
                return@forEach
            }

            val matches = mutableListOf<FuseMatch>()

            keys.forEachIndexed { keyIndex, key ->
                val value = item!![keyIndex]
                if (value != null) {
                    matches.addAll(_findMatches(key, value, searcher))
                }
            }

            if (matches.isNotEmpty()) {
                results.add(
                    FuseResult<T>(
                        idx = record.i,
                        item = _docs[record.i],
                        matches = matches
                    )
                )
            }
        }

        return results
    }

    private fun _findMatches(key: FuseKey, value: Any, searcher: BitapSearch): List<FuseMatch> {
        if (!isDefined(value)) {
            return emptyList()
        }

        val matches = mutableListOf<FuseMatch>()

        when (value) {
            is List<*> -> {
                @Suppress("UNCHECKED_CAST")
                val subRecords = value as List<SubRecord>
                subRecords.forEach { subRecord ->
                    val text = subRecord.v
                    if (!isDefined(text)) {
                        return@forEach
                    }

                    val searchResult = searcher.searchIn(text)

                    if (searchResult.isMatch) {
                        matches.add(
                            FuseMatch(
                                score = searchResult.score,
                                key = key.src.toString(),
                                value = text,
                                idx = subRecord.i,
                                norm = subRecord.n,
                                indices = searchResult.indices
                            )
                        )
                    }
                }
            }
            is SubRecord -> {
                val text = value.v
                val searchResult = searcher.searchIn(text)

                if (searchResult.isMatch) {
                    matches.add(
                        FuseMatch(
                            score = searchResult.score,
                            key = key.src.toString(),
                            value = text,
                            norm = value.n,
                            indices = searchResult.indices
                        )
                    )
                }
            }
        }

        return matches
    }
}