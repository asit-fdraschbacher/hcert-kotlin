@file:JsModule("cose-js")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")
package cose

import Buffer
import kotlin.js.*

external interface CosePublicKey {}

external interface EcCosePublicKey : CosePublicKey {
    val x: Buffer
    val y: Buffer
}
external interface RsaCosePublicKey : CosePublicKey {
    val n: Buffer
    val e: Number
}

external interface CosePrivateKey

external interface EcCosePrivateKey : CosePrivateKey {
    val d: Buffer
}

external interface Verifier {
    val key: CosePublicKey
}

external interface AlgorithmHeader {
    val alg: String
}

external interface KidHeader {
    val kid: String
}

external interface Headers {
    val p: AlgorithmHeader
    val u: KidHeader
}

external interface Signer {
    val key: CosePrivateKey
}
external open class sign {
    companion object {
        fun verifySync(message: Buffer, verifier: Verifier): Buffer
        fun createSync(headers: dynamic, data: Buffer, signer: Signer): Buffer
    }
}