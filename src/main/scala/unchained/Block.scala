package unchained

import akka.NotUsed
import akka.stream.scaladsl.{ Flow, Source }
import akka.util.ByteString

import scala.util.{ Success, Try, Failure }

/*

Protocol specification:
  - https://en.bitcoin.it/wiki/Protocol_documentation#block
*/

final case class BlockHeader(
  version: Int,
  prevBlock: Block.Hash, // 	The hash value of the previous block this particular block references
  merkleRoot: Block.Hash, // 	The reference to a Merkle tree collection which is a hash of all transactions related to this block
  timestamp: Long, //	A Unix timestamp recording when this block was created (Currently limited to dates before the year 2106!)
  bits: Long, //  The calculated difficulty target being used for this block
  nonce: Long //	The nonce used to generate this block… to a
)

object BlockHeader {
  val VERSION_SIZE = 4
  val PREV_BLOCK_SIZE = 32
  val MERKLE_ROOT_SIZE = 32
  val TIMESTAMP_SIZE = 4
  val BITS_SIZE = 4
  val NONCE_SIZE = 4

  def parse: Flow[ByteString, Prefixed[BlockHeader, ByteString], NotUsed] =
    Flow[ByteString]
      .via(Streams.bytesPrefixAndTail(Block.HEADER_SIZE))
      .via(parsePrefixed)

  def parsePrefixed: Flow[Prefixed[ByteString, ByteString], Prefixed[BlockHeader, ByteString], NotUsed] =
    Flow[Prefixed[ByteString, ByteString]].flatMapConcat { prefixed =>
      BlockHeader(prefixed.head) match {
        case Success(parsedHeader) => Source.single(Prefixed(parsedHeader, prefixed.tail))
        case Failure(e) => Source.failed(e)
      }
    }

  def apply(in: ByteString): Try[BlockHeader] = Try {
    val v = in.splitAt(VERSION_SIZE)
    val version = v._1.utf8String.toInt

    val p = v._2.splitAt(PREV_BLOCK_SIZE)
    val prevBlock = p._1.utf8String

    val m = p._2.splitAt(MERKLE_ROOT_SIZE)
    val merkleRoot = m._1.utf8String

    val t = m._2.splitAt(TIMESTAMP_SIZE)
    val timestamp = t._1.utf8String.toLong

    val b = t._2.splitAt(BITS_SIZE)
    val bits = b._1.utf8String.toLong

    val nonce = b._2.take(NONCE_SIZE).utf8String.toLong

    BlockHeader(version, Block.Hash(prevBlock), Block.Hash(merkleRoot), timestamp, bits, nonce)
  }
}

object Block {
  val HEADER_SIZE = 40 // header size represented in bytes

  final case class Hash(value: String) extends AnyVal

  def headerWithPayload = BlockHeader.parse _
}

object Streams {

  def bytesPrefixAndTail(exactly: Int, acc: ByteString = ByteString.empty): Flow[ByteString, Prefixed[ByteString, ByteString], NotUsed] = {
    Flow[ByteString].prefixAndTail(1).flatMapConcat {
      case (prefix: ByteString, tail: Source[ByteString, NotUsed]) =>
        val bytes: ByteString = acc ++ prefix

        // chunk is larger than needed bytes
        // adjust tail by prepending the extra bytes into the original source
        if (bytes.length >= exactly) {
          val head = bytes.take(exactly)
          val remaining = bytes.drop(exactly)
          val newSource = Source.single(remaining) ++ tail
          val prefixed = Prefixed[ByteString, ByteString](head, newSource)

          Source.single(prefixed)
        } else tail.via(bytesPrefixAndTail(exactly, acc))

      case _ => Source.failed(new Exception("[-] Error parsing block: Could not get block header"))
    }
  }
}

case class Prefixed[A, B](head: A, tail: Source[B, NotUsed])

