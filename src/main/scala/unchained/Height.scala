package unchained

import akka.NotUsed
import akka.util.ByteString

import akka.stream.scaladsl.{ Flow, Source }

/**
 * [[https://bitcoin.org/en/glossary/block-height Block height]]
 *
 * @param value unsigned integer representation
 */
class Height(val value: BigInt) extends AnyVal {
  @inline override def toString = value.toString
}

object Height {
  /**
   * Returns the [[Height]] representation for the given numeric value.
   */
  def apply[N: Numeric](numeric: N): Height =
    new Height(BigInt(implicitly[Numeric[N]] toLong numeric))

  /**
   * Block [[Height]] parser from the given binary representation.
   */
  def parser: Flow[ByteString, Height, NotUsed] =
    Flow[ByteString].flatMapConcat[Height, NotUsed] { repr =>
      val buf = repr.asByteBuffer

      if (!buf.hasRemaining) {
        Source.failed[Height](new IllegalArgumentException(
          "Empty binary representation; Cannot find even value length"))

      } else {
        val len = buf.order(java.nio.ByteOrder.LITTLE_ENDIAN).get()

        Serialization.variableUnsigned(len, repr.tail).
          map { new Height(_) }
      }
    }

}
