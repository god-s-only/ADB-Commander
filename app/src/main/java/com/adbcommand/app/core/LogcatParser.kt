package com.adbcommand.app.core

import com.adbcommand.app.domain.models.LogLevel
import com.adbcommand.app.domain.models.LogLine

object LogcatParser {

    private val THREADTIME_REGEX = Regex(
        """^(\d{2}-\d{2}\s\d{2}:\d{2}:\d{2}\.\d+)\s+\d+\s+\d+\s+([VDIWEFS])\s+(.*?)\s*:\s(.*)$"""
    )

    private val BRIEF_REGEX = Regex(
        """^([VDIWEFS])/(.+?)\(\s*\d+\):\s(.*)$"""
    )

    fun parse(raw: String): LogLine {
        if (raw.isBlank()) return LogLine(id = 0, raw = raw)

        THREADTIME_REGEX.find(raw)?.let { match ->
            val (timestamp, levelChar, tag, message) = match.destructured
            return LogLine(
                id = 0,
                raw = raw,
                timestamp = timestamp.trim(),
                level = LogLevel.fromChar(levelChar),
                tag = tag.trim(),
                message = message.trim()
            )
        }
        BRIEF_REGEX.find(raw)?.let { match ->
            val (levelChar, tag, message) = match.destructured
            return LogLine(
                id = 0,
                raw     = raw,
                level   = LogLevel.fromChar(levelChar),
                tag     = tag.trim(),
                message = message.trim()
            )
        }
        return LogLine(id = 0, raw = raw, message = raw)
    }
}