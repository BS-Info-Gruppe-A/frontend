@file:OptIn(ExperimentalSerializationApi::class)

package eu.bsinfo.util

import app.softwork.serialization.csv.CSVFormat
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML

private val json = Json {
    prettyPrint = true
}
private val csv = CSVFormat
private val xml = XML {
    indentString = " ".repeat(4)
}

val formats = mapOf(
    "json" to json,
    "csv" to csv,
    "xml" to xml
)

fun <T> List<T>.export(format: StringFormat, path: Path, serializer: KSerializer<T>) {
    val output = format.encodeToString(ListSerializer(serializer), this)
    SystemFileSystem.sink(path).buffered().use {
        it.writeString(output)
    }
}

suspend fun <T> FileHandle.import(format: StringFormat, serializer: KSerializer<T>): List<T> =
    format.decodeFromString(ListSerializer(serializer), read())

