package ehn.techiop.hcert.kotlin.trust

import ehn.techiop.hcert.kotlin.chain.CertificateRepository
import ehn.techiop.hcert.kotlin.crypto.CoseHeaderKeys
import ehn.techiop.hcert.kotlin.crypto.CwtHeaderKeys
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlin.time.Duration

class TrustListDecodeService(private val repository: CertificateRepository, private val clock: Clock = Clock.System) {

    fun decode(input: ByteArray, optionalContent: ByteArray? = null): List<TrustedCertificate> {
        val coseAdapter = CoseAdapter(input)
        val kid = coseAdapter.getProtectedAttributeByteArray(CoseHeaderKeys.KID.value)
            ?: throw IllegalArgumentException("KID")
        val validated = coseAdapter.validate(kid, repository)
        if (!validated) throw IllegalArgumentException("signature")
        val version = coseAdapter.getProtectedAttributeInt(42)
        if (version == 1) {
            throw IllegalArgumentException("V1")
        } else if (version == 2 && optionalContent != null) {
            val actualHash = Hash(optionalContent).calc()

            val map = coseAdapter.getContentMap()
            val expectedHash = map.getByteArray(CwtHeaderKeys.SUBJECT.value)
            if (!(expectedHash contentEquals actualHash))
                throw IllegalArgumentException("Hash")

            val notBefore = map.getNumber(CwtHeaderKeys.NOT_BEFORE.value)
                ?: throw IllegalArgumentException("ValidFrom")
            val validFrom = Instant.fromEpochSeconds(notBefore.toLong())
            if (validFrom > clock.now().plus(Duration.seconds(300)))
                throw IllegalArgumentException("ValidFrom")

            val expiration = map.getNumber(CwtHeaderKeys.EXPIRATION.value)
                ?: throw IllegalArgumentException("ValidUntil")
            val validUntil = Instant.fromEpochSeconds(expiration.toLong())
            if (validUntil < clock.now().minus(Duration.seconds(300)))
                throw IllegalArgumentException("ValidUntil")

            return Cbor.decodeFromByteArray<TrustListV2>(optionalContent).certificates
        } else {
            throw IllegalArgumentException("version")
        }
    }

}

