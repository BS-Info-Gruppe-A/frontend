@file:OptIn(ExperimentalSerializationApi::class)

package eu.bsinfo.cli.data

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

enum class Format(val format: StringFormat) {
    XML(xml),
    CSV(csv),
    JSON(json),
}