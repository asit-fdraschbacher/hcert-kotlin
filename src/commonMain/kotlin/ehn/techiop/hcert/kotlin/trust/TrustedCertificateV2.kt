package ehn.techiop.hcert.kotlin.trust

import ehn.techiop.hcert.kotlin.crypto.Certificate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.cbor.ByteString


@Serializable
data class TrustedCertificateV2(
    @SerialName("i")
    @ByteString
    override val kid: ByteArray,

    @SerialName("c")
    @ByteString
    val certificate: ByteArray
) : TrustedCertificate {

    //**WARNING*** do not use lazy delegates! it *will* break!!!

    @Transient
    val decodedCertificate = decodeCertificate()

    @Transient
    override val validContentTypes = decodedCertificate.validContentTypes

    @Transient
    override val cosePublicKey = decodedCertificate.publicKey

    @Transient
    override val validUntil = decodedCertificate.validUntil

    @Transient
    override val validFrom = decodedCertificate.validFrom

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        other ?: let { if (this::class != it::class) return false }

        other as TrustedCertificateV2

        if (!kid.contentEquals(other.kid)) return false
        if (!certificate.contentEquals(other.certificate)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kid.contentHashCode()
        result = 31 * result + certificate.contentHashCode()
        return result
    }
}


expect fun TrustedCertificateV2.decodeCertificate(): Certificate<*>