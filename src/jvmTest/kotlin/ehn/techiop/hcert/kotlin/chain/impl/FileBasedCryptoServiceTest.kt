package ehn.techiop.hcert.kotlin.chain.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class FileBasedCryptoServiceTest : FunSpec({
    withData(256, 384) { keySize ->
        val input = RandomEcKeyCryptoService(keySize)
        val privateKeyPem = input.exportPrivateKeyAsPem()
        val certificatePem = input.exportCertificateAsPem()
        val parsed = FileBasedCryptoService(privateKeyPem, certificatePem)

        input.getCborSigningKey().oneKey.EncodeToBytes() shouldBe (parsed.getCborSigningKey().oneKey.EncodeToBytes())
        input.getCertificate().kid shouldBe (parsed.getCertificate().kid)
    }
    withData(2048, 3072) { keySize ->
        val input = RandomRsaKeyCryptoService(keySize)
        val privateKeyPem = input.exportPrivateKeyAsPem()
        val certificatePem = input.exportCertificateAsPem()
        val parsed = FileBasedCryptoService(privateKeyPem, certificatePem)

        input.getCborSigningKey().oneKey.EncodeToBytes() shouldBe (parsed.getCborSigningKey().oneKey.EncodeToBytes())
        input.getCertificate().kid shouldBe (parsed.getCertificate().kid)
    }
})