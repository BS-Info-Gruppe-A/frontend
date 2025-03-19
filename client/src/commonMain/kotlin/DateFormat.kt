package eu.bsinfo.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias SerializableDate = @Serializable(with = DateSerializer::class) LocalDate

private val format = LocalDate.Format {
    year()
    char('-')
    monthNumber(Padding.ZERO)
    char('-')
    dayOfMonth(Padding.ZERO)
}

object DateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("eu.bsinfo.LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) =
        encoder.encodeString(format.format(value))

    override fun deserialize(decoder: Decoder): LocalDate =
        format.parse(decoder.decodeString())
}
