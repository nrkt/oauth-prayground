package nrkt.oidc.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ClientId.StringAsClientIdValueSerializer::class)
data class ClientId(
    val value: String,
) {
    override fun toString(): String {
        return value
    }

    object StringAsClientIdValueSerializer : KSerializer<ClientId> {
        override val descriptor = PrimitiveSerialDescriptor("ClientId", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: ClientId) {
            encoder.encodeString(value.value)
        }

        override fun deserialize(decoder: Decoder): ClientId {
            return ClientId(decoder.decodeString())
        }
    }
}