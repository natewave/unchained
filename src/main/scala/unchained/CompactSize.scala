package unchained

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

        case Some(byte) if (byte >= Int8 && byte < UInt16) => // 1 byte
          Source.single(new CompactSize(BigInt((byte & 0xFF).toInt)))

        case Some(varSz) => {
          val varLen: Byte = { // byte count
            if (varSz == UInt16) 2
            else if (varSz == UInt32) 4
            else if (varSz == UInt64) 8
            else 0
          }

          if (varLen == 0) {
            Source.failed[CompactSize](new IllegalArgumentException(
              s"Invalid CompactSize length: $varSz"))
          } else {
            Serialization.variableUnsigned(
              varLen, repr.tail).map { new CompactSize(_) }
          }
        }

        case _ => Source.empty[CompactSize]
      }
    }

  // ---

  private val Int8 = 0x80.toByte // short 128 as byte
  private val UInt16 = 0xFD.toByte
  private val UInt32 = 0xFE.toByte
  private val UInt64 = 0xFF.toByte
}
