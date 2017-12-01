package unchained

import java.nio.ByteOrder.LITTLE_ENDIAN

import akka.NotUsed

import akka.util.ByteString

import akka.stream.scaladsl.Source

private[unchained] object Serialization {
  /**
   * Parses a [[CompactSize]] from a variable length representation.
   *
   * @param length the expected byte count
   * @param data the binary data
   */
  def variableUnsigned(length: Byte, data: ByteString): Source[BigInt, NotUsed] = {
    def byteBuf(d: ByteString) = d.asByteBuffer.order(LITTLE_ENDIAN)

    if (length == 1) {
      val buf = byteBuf(data :+ (0x00: Byte))
      def int: Int = (buf.getShort & /*unsigned*/ 0x0000FFFF).toInt

      Source.single(BigInt(int))
    } else if (length == 2) {
      def int: Int = (byteBuf(data).getShort & /*unsigned*/ 0x0000FFFF).toInt

      Source.single(BigInt(int))
    } else if (length == 3) {
      val buf = byteBuf(data :+ (0x00: Byte))
      def long: Long = (buf.getInt & /*unsigned*/ 0x00000000FFFFFFFF).toLong

      Source.single(BigInt(long))
    } else if (length == 4) {
      def long: Long = (byteBuf(data).
        getInt & /*unsigned*/ 0x00000000FFFFFFFF).toLong

      Source.single(BigInt(long))
      // TODO: if length < 8?
    } else if (length == 8) {
      Source.single(BigInt(byteBuf(data).getLong) & uint64Mask)
    } else {
      Source.failed[BigInt](new IllegalArgumentException(
        s"Invalid length: $length"))
    }
  }

  // ---

  // Bitwise mask to shift as uint64 represented by a BitInt
  private val uint64Mask = BigInt(Array[Byte](0, 0, 0, 0, 0, 0, 0, 0, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte))
}
