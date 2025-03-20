@file:OptIn(ExperimentalSerializationApi::class)

package eu.bsinfo.data

import app.softwork.serialization.csv.CSVFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML

private val xml = XML {
    indentString = " ".repeat(4)
}

private val json = Json {
    prettyPrint = true
}

private val csv = CSVFormat

enum class Format(val extension: String, val format: StringFormat) : StringFormat by format {
    XML("xml", xml),
    CSV("csv", csv),
    JSON("json", json);

    companion object {
        val extensions = entries.map { it.extension }
        fun fromExtension(extension: String): Format? = entries.find { it.extension == extension }
    }
}
