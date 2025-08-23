package com.whatdoiputhere.iptvplayer.parser

import com.whatdoiputhere.iptvplayer.model.Channel

class M3UParser {
    fun parse(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.split("\n")

        var currentChannel: Channel? = null
        var nextIsUrl = false

        for (line in lines) {
            val trimmedLine = line.trim()

            if (trimmedLine.startsWith("#EXTINF:")) {
                currentChannel = parseExtinf(trimmedLine)
                nextIsUrl = true
            } else if (nextIsUrl && trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                currentChannel?.let { channel ->
                    channels.add(channel.copy(url = trimmedLine))
                }
                currentChannel = null
                nextIsUrl = false
            }
        }

        return channels
    }

    private fun parseExtinf(line: String): Channel {
        val name = extractChannelName(line)
        val logo = extractAttribute(line, "tvg-logo")
        val group = extractAttribute(line, "group-title")
        val language = extractAttribute(line, "tvg-language")
        val country = extractAttribute(line, "tvg-country")
        val id = extractAttribute(line, "tvg-id")

        return Channel(
            id = id,
            name = name,
            url = "",
            logo = logo,
            group = group,
            language = language,
            country = country,
        )
    }

    private fun extractChannelName(line: String): String {
        val lastCommaIndex = line.lastIndexOf(',')
        return if (lastCommaIndex != -1 && lastCommaIndex < line.length - 1) {
            line.substring(lastCommaIndex + 1).trim()
        } else {
            "Unknown Channel"
        }
    }

    private fun extractAttribute(
        line: String,
        attribute: String,
    ): String =
        try {
            val pattern = "$attribute=\"([^\"]*)\""
            val regex = Regex(pattern)
            val matchResult = regex.find(line)
            matchResult?.groupValues?.getOrNull(1) ?: ""
        } catch (e: Exception) {
            val startPattern = "$attribute=\""
            val startIndex = line.indexOf(startPattern)
            if (startIndex != -1) {
                val valueStart = startIndex + startPattern.length
                val valueEnd = line.indexOf("\"", valueStart)
                if (valueEnd != -1) {
                    line.substring(valueStart, valueEnd)
                } else {
                    ""
                }
            } else {
                ""
            }
        }
}
