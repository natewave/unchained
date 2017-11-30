package io.github.natewave

import akka.NotUsed
import akka.util.ByteString

import akka.stream.scaladsl.{ Flow, Source }

/**
 * [[https://bitcoin.org/en/developer-reference#compactsize-unsigned-integers Compact size]]
 *
 * @param value unsigned integer representation
 */
class CompactSize(val value: BigInt) extends AnyVal {
  @inline override def toString = value.toString
}

object CompactSize {
  /**
   * Returns the [[CompactSize]] representation for the given numeric value.
   */
  def apply[N: Numeric](numeric: N): CompactSize =
    new CompactSize(BigInt(implicitly[Numeric[N]] toLong numeric))

  /**
   * [[CompactSize]] parser from the given binary representation.
   */
  def parser: Flow[ByteString, CompactSize, NotUsed] =
    Flow[ByteString].flatMapConcat[CompactSize, NotUsed] { repr =>
      repr.headOption match {
        case Some(byte) if (byte >= 0) => // 1 byte up to 127
          Source.single(new CompactSize(BigInt(byte.toInt)))

        case Some(byte) if (byte >= lower && byte < UInt16) => // 1 byte
          Source.single(new CompactSize(BigInt((byte & 0xFF).toInt)))

        case Some(varLen) =>
          variableLength(varLen, repr.tail).map { new CompactSize(_) }

        case _ => Source.empty[CompactSize]
      }
    }

  // ---

  private val lower = 0x80.toByte // short 128 as byte

  private val UInt16 = 0xFD.toByte
  private val UInt32 = 0xFE.toByte
  private val UInt64 = 0xFF.toByte

  import java.nio.ByteOrder.LITTLE_ENDIAN

  private val uint64Mask = BigInt(Array[Byte](0, 0, 0, 0, 0, 0, 0, 0, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte))

  /**
   * Parses a [[CompactSize]] from a variable length representation.
   *
   * @param length the expected length
   * @param data the binary data
   */
  private def variableLength(length: Byte, data: ByteString): Source[BigInt, NotUsed] = {
    def byteBuf = data.asByteBuffer.order(LITTLE_ENDIAN)

    if (length == UInt16) {
      def int: Int = (byteBuf.getShort & /*unsigned*/ 0x0000FFFF).toInt

      if (data.size == 2) {
        Source.single(BigInt(int))
      } else {
        Source.failed[BigInt](new IllegalArgumentException(
          s"Invalid CompactSize length: ${data.size} != 2 (uint16)"))
      }
    } else if (length == UInt32) {
      def long: Long = (byteBuf.getInt & /*unsigned*/ 0x00000000FFFFFFFF).toLong
      if (data.size == 4) {
        Source.single(BigInt(long))
      } else {
        Source.failed[BigInt](new IllegalArgumentException(
          s"Invalid CompactSize length: ${data.size} != 4 (uint32)"))
      }
    } else if (length == UInt64) {
      if (data.size == 8) {
        Source.single(BigInt(byteBuf.getLong) & uint64Mask)
      } else {
        Source.failed[BigInt](new IllegalArgumentException(
          s"Invalid CompactSize length: ${data.size} != 8 (uint64)"))
      }
    } else {
      Source.failed[BigInt](new IllegalArgumentException(
        s"Invalid CompactSize length: $length"))
    }
  }
}
