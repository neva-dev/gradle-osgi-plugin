package com.neva.gradle.osgi.internal

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.ISO8601Utils
import org.apache.commons.lang3.time.DurationFormatUtils
import java.util.*

object Formats {

    val JSON_MAPPER = {
        val printer = DefaultPrettyPrinter()
        printer.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)

        ObjectMapper().writer(printer)
    }()

    fun toJson(value: Any): String? {
        return JSON_MAPPER.writeValueAsString(value)
    }

    fun <T> fromJson(json: String, clazz: Class<T>): T? {
        return ObjectMapper().readValue(json, clazz)
    }

    fun toBase64(value: String): String {
        return Base64.getEncoder().encodeToString(value.toByteArray())
    }

    fun bytesToHuman(bytes: Long): String {
        if (bytes < 1024) {
            return bytes.toString() + " B"
        } else if (bytes < 1024 * 1024) {
            return (bytes / 1024).toString() + " KB"
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    fun percent(current: Int, total: Int): String {
        return percent(current.toLong(), total.toLong())
    }

    fun percent(current: Long, total: Long): String {
        val value: Double = if (total == 0L) 0.0 else current.toDouble() / total.toDouble()
        return "${"%.2f".format(value * 100.0)}%"
    }

    fun duration(millis: Long): String {
        return DurationFormatUtils.formatDurationHMS(millis)
    }

}