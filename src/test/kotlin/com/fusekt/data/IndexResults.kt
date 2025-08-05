package com.fusekt.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias IndexResult = List<IndexResultItem>

@Serializable
data class IndexResultItem(
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("author_uid") val authorUid: String? = null,
    @SerialName("body") val body: String? = null,
    @SerialName("tags") val tags: List<String?>? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("url") val url: String? = null
)