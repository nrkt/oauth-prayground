package nrkt.oidc.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = UserId.StringAsUserIdValueSerializer::class)
data class UserId(
    val value: String,
) {
    override fun toString(): String {
        return value
    }

    object StringAsUserIdValueSerializer : KSerializer<UserId> {
        override val descriptor = PrimitiveSerialDescriptor("UserId", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: UserId) {
            encoder.encodeString(value.value)
        }

        override fun deserialize(decoder: Decoder): UserId {
            return UserId(decoder.decodeString())
        }
    }
}