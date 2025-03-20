@file:OptIn(ExperimentalSerializationApi::class)

package eu.bsinfo.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer

suspend fun <T> FileHandle.import(format: StringFormat, serializer: KSerializer<T>): List<T> =
    format.decodeFromString(ListSerializer(serializer), read())

