package com.fusekt

import com.fusekt.core.*
import com.fusekt.helpers.*
import com.fusekt.search.bitap.BitapSearch
import com.fusekt.tools.*
import com.fusekt.transform.FormattedResult

class Fuse(
    docs: List<Any>,
    options: FuseOptions = FuseOptions(),
    index: FuseIndex? = null
) {
    private val options: FuseOptions = options
    private val _keyStore: KeyStore = KeyStore(options.keys)
    private var _docs: MutableList<Any> = docs.toMutableList()
    private var _myIndex: FuseIndex = index ?: createIndex(options.keys, docs, options)

    companion object {
        const val version = "1.0.0"
        
        fun createIndex(keys: List<Any>, docs: List<Any>, options: FuseOptions = FuseOptions()): FuseIndex {
            return com.fusekt.tools.createIndex(keys, docs, options)
        }
        
        fun parseIndex(data: Map<String, Any>, options: FuseOptions = FuseOptions()): FuseIndex {
            return com.fusekt.tools.parseIndex(data, options)
        }
    }

    init {
        if (options.useExtendedSearch) {
            throw UnsupportedOperationException("Extended search is not yet implemented")
        }

        setCollection(docs, index)
    }

    private fun setCollection(docs: List<Any>, index: FuseIndex? = null) {
        _docs = docs.toMutableList()

        if (index != null && index !is FuseIndex) {
            throw IllegalArgumentException("Incorrect index type")
        }

        _myIndex = index ?: createIndex(
            keys = options.keys,
            docs = _docs,
            options = options
        )
    }

    fun add(doc: Any) {
        if (!isDefined(doc)) {
            return
        }

        _docs.add(doc)
        _myIndex.add(doc)
    }

    fun remove(predicate: (Any, Int) -> Boolean = { _, _ -> false }): List<Any> {
        val results = mutableListOf<Any>()

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

    fun removeAt(idx: Int) {
        _docs.removeAt(idx)
        _myIndex.removeAt(idx)
    }

    fun getIndex(): FuseIndex = _myIndex

    fun search(query: Any, limit: Int = -1): List<FormattedResult> {
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

        computeScore(results.toMutableList(), options.ignoreFieldNorm)

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

        return format(
            results = limitedResults,
            docs = _docs,
            includeMatches = options.includeMatches,
            includeScore = options.includeScore
        )
    }

    private fun _searchStringList(query: String): List<FuseResult> {
        val searcher = BitapSearch(query, options)
        val records = _myIndex.records
        val results = mutableListOf<FuseResult>()

        records.forEach { record ->
            val text = record.v
            if (!isDefined(text)) {
                return@forEach
            }

            val searchResult = searcher.searchIn(text!!)

            if (searchResult.isMatch) {
                results.add(
                    FuseResult(
                        item = text,
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

    private fun _searchObjectList(query: String): List<FuseResult> {
        val searcher = BitapSearch(query, options)
        val keys = _myIndex.keys
        val records = _myIndex.records
        val results = mutableListOf<FuseResult>()

        records.forEach { record ->
            val item = record.`$`
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
                    FuseResult(
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