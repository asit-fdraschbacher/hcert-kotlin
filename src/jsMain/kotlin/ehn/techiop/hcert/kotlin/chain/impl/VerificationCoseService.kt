package ehn.techiop.hcert.kotlin.chain.impl

import Buffer
import ehn.techiop.hcert.kotlin.chain.*
import ehn.techiop.hcert.kotlin.crypto.Cose
import ehn.techiop.hcert.kotlin.trust.buildCosePublicKey
import org.khronos.webgl.Uint8Array

actual class VerificationCoseService actual constructor(private val repository: CertificateRepository) : CoseService {

    override fun encode(input: ByteArray) = throw NotImplementedError()

    override fun decode(input: ByteArray, verificationResult: VerificationResult): ByteArray {
        verificationResult.coseVerified = false
        verificationResult.coseVerified = false

        val cborJson = Cbor.Decoder.decodeAllSync(Buffer.from(input.toUint8Array()))
        val cwt = cborJson[0] as Cbor.Tagged
        val cwtValue = cwt.value as Array<Buffer>
        val protectedHeader = cwtValue[0]
        val unprotectedHeader = cwtValue[1]
        val content = cwtValue[2]
        val signature = cwtValue[3]

        // TODO: Can we get rid of dynamic here?
        val protectedHeaderCbor = Cbor.Decoder.decodeAllSync(protectedHeader)[0].asDynamic()
        val kid = protectedHeaderCbor.get(4) as Uint8Array? ?: if (unprotectedHeader.length !== undefined)
            // TODO: Does this work?
            Cbor.Decoder.decodeAllSync(unprotectedHeader).asDynamic().get(1) as Uint8Array
        else
            throw IllegalArgumentException("KID not found")
        if (kid === undefined)
            throw IllegalArgumentException("KID not found")

        val algorithm = protectedHeaderCbor.get(1)

        repository.loadTrustedCertificates(kid.toByteArray(), verificationResult).forEach { trustedCert ->
            verificationResult.certificateValidFrom = trustedCert.validFrom
            verificationResult.certificateValidUntil = trustedCert.validUntil
            verificationResult.certificateValidContent = trustedCert.validContentTypes
            val pubKey = trustedCert.buildCosePublicKey()
            val result = Cose.verify(input, pubKey)
            console.info("COSE VERIFIED")
            console.info(result)
            verificationResult.coseVerified = true
            return@forEach
        }

        return content.toByteArray()
    }
}