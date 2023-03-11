package com.m3u.data.local.source.analyzer.m3u

import com.m3u.core.util.basic.trimBrackets
import com.m3u.core.util.collection.loadLine
import com.m3u.data.local.source.analyzer.Analyzer
import com.m3u.data.local.source.model.M3U
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.stream.Stream
import kotlin.streams.asSequence

class M3UAnalyzer internal constructor() : Analyzer<List<M3U>>() {
    @Volatile
    private var block: M3U? = null
    private val list = mutableListOf<M3U>()

    override suspend fun analyze(lines: Stream<String>) {
        withContext(Dispatchers.Default) {
            lines.asSequence()
                .filterNot { it.isEmpty() }
                .forEach { line ->
                    when {
                        line.startsWith(M3U_HEADER_MARK) -> reset()
                        line.startsWith(M3U_INFO_MARK) -> {
                            block?.let {
                                list.add(it)
                            }
                            block = M3U().setContent(line)
                        }

                        else -> {
                            block = block?.setUrl(line)
                            block?.let {
                                list.add(it)
                            }
                            block = null
                        }
                    }
                }
        }
    }

    override fun get(): List<M3U> = list

    override fun reset() {
        list.clear()
        block = null
    }

    private fun M3U.setUrl(url: String): M3U = copy(url = url)

    private fun M3U.setContent(content: String): M3U {
        val properties = makeProperties(content)
        if (properties.isEmpty) {
            val title = content.split(",").last()
            return this.copy(
                title = title
            )
        } else {
            val id = properties.getProperty(M3U_TVG_ID_MARK, "")
            val name = properties.getProperty(M3U_TVG_NAME_MARK, "")
            val cover = properties.getProperty(M3U_TVG_LOGO_MARK, "")

            val groupTitle = properties.getProperty(M3U_GROUP_TITLE_MARK, ",").split(",")

            val (group, title) = if (groupTitle.size == 2) {
                groupTitle.first() to groupTitle[1]
            } else {
                "" to groupTitle.firstOrNull().orEmpty()
            }
            return this.copy(
                id = id.trimBrackets(),
                name = name.trimBrackets(),
                group = group.trimBrackets(),
                title = title.trimBrackets(),
                cover = cover.trimBrackets()
            )
        }


    }

    private val String.hasLegalMark: Boolean
        get() = startsWith(M3U_TVG_ID_MARK) ||
                startsWith(M3U_TVG_LOGO_MARK) ||
                startsWith(M3U_TVG_NAME_MARK) ||
                startsWith(M3U_GROUP_TITLE_MARK)

    private fun makeProperties(content: String): Properties {
        val properties = Properties()
        val parts = content.split(" ")
        // check each of parts
        var illegalPart: String? = null
        for (part in parts.reversed()) {
            if (part.startsWith(M3U_INFO_MARK)) continue
            val mergedPart = part + illegalPart.orEmpty()
            illegalPart = if (mergedPart.hasLegalMark) {
                properties.loadLine(mergedPart)
                null
            } else {
                mergedPart + illegalPart.orEmpty()
            }
        }
        if (properties.isEmpty) {
            return properties
        }
        if (illegalPart != null) {
            val split = illegalPart.split(",")
            if (split.isNotEmpty()) {
                val title = if ((split.size == 1)) split[0] else split[1]
                properties[M3U_GROUP_TITLE_MARK] = title
            } else {
                error("illegal m3u content: $content")
            }
        }
        return properties
    }

    companion object {
        private const val M3U_HEADER_MARK = "#EXTM3U"
        private const val M3U_INFO_MARK = "#EXTINF:"

        private const val M3U_TVG_LOGO_MARK = "tvg-logo"
        private const val M3U_TVG_ID_MARK = "tvg-id"
        private const val M3U_TVG_NAME_MARK = "tvg-name"
        private const val M3U_GROUP_TITLE_MARK = "group-title"
    }
}