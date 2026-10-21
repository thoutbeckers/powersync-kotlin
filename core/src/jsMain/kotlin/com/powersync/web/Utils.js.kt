@file:OptIn(ExperimentalWasmJsInterop::class)

package com.powersync.web

import com.powersync.internal.InternalPowerSyncAPI

internal actual fun Long.toBigInt(): JsAny = composeBigInt((this shr 32).toInt().toJsNumber(), toInt().toJsNumber(), i32BigInt)

internal actual fun JsAny.bigIntToLong(): Long {
    // We can't represent big integers in Kotlin directly, extract high and low i32 values to
    // compose the long value.
    val high = bigIntInt32(bigIntShiftRight(this, i32BigInt)).toLong()
    val low = bigIntInt32(this).toLong()
    return (high shl 32) or (low and 0xFFFFFFFFL)
}

private val i32BigInt = bigInt(32.toJsNumber())

private fun bigIntShiftRight(
    a: JsAny,
    b: JsAny,
) = js("a >> b")

// Truncate in BigInt before converting: Number(bigInt) alone rounds values beyond 2^53.
private fun bigIntInt32(bigInt: JsAny): JsNumber = js("Number(BigInt.asIntN(32, bigInt))")

// `>>> 0` reads the low half as unsigned, so its sign bit doesn't spill into the high half.
private fun composeBigInt(
    high: JsNumber,
    low: JsNumber,
    shift: JsAny,
): JsAny = js("(BigInt(high) << shift) | BigInt(low >>> 0)")

@InternalPowerSyncAPI
internal actual fun Uint8Array.asByteArray(): ByteArray {
    // Kotlin uses Int8Array as a byte array representation, so we just need to convert.
    val array = this
    return js("new Int8Array(array.buffer, array.byteOffset, array.byteLength)")
}

@InternalPowerSyncAPI
internal actual fun ByteArray.copyAsArrayBuffer(length: Int): ArrayBuffer {
    val source = this
    return js("source.slice(0, length).buffer")
}
