package unchained

import scala.util.{ Success, Try, Failure }

import akka.stream.scaladsl.{ Flow, Source }
import akka.util.ByteString

import Parsing.{ Prefixed, Binary, Hash }

/*

Protocol specification:
  - https://en.bitcoin.it/wiki/Protocol_documentation#block
*/

final case class BlockHeader(
  version: Int,
  prevBlock: Hash, // 	The hash value of the previous block this particular block references
  merkleRoot: Hash, // 	The reference to a Merkle tree collection which is a hash of all transactions related to this block
  timestamp: Int, //	A Unix timestamp recording when this block was created (Currently limited to dates before the year 2106!)
  bits: Int, //  The calculated difficulty target being used for this block
  nonce: Int //	The nonce used to generate this block… to a
)

object BlockHeader {
  val MAGIC_NO_SIZE = 4
  val BLOCK_SIZE_SIZE = 4
  val VERSION_SIZE = 4
  val PREV_BLOCK_SIZE = 32
  val MERKLE_ROOT_SIZE = 32
  val TIMESTAMP_SIZE = 4
  val BITS_SIZE = 4
  val NONCE_SIZE = 4

  def parser: Flow[ByteString, Prefixed[BlockHeader, ByteString], _] =
    Flow[ByteString]
      .via(Parsing.bytesPrefixAndTail(Block.METADATA_SIZE))
      .via(parsePrefixed)

  def parsePrefixed: Flow[Prefixed[ByteString, ByteString], Prefixed[BlockHeader, ByteString], _] =
    Flow[Prefixed[ByteString, ByteString]].flatMapConcat { prefixed =>
      val header = prefixed.head.drop(Block.SKIP_SIZE)

      BlockHeader(header) match {
        case Success(parsedHeader) => Source.single(Prefixed(parsedHeader, prefixed.tail))
        case Failure(e) => Source.failed(e)
      }
    }

  def apply(in: ByteString): Try[BlockHeader] = Try {
    val v = in.splitAt(VERSION_SIZE)
    val version = Binary.getInt(v._1)

    val p = v._2.splitAt(PREV_BLOCK_SIZE)
    val prevBlock = Hash(p._1.toArray)

    val m = p._2.splitAt(MERKLE_ROOT_SIZE)
    val merkleRoot = Hash(m._1.toArray)

    val t = m._2.splitAt(TIMESTAMP_SIZE)
    val timestamp = Binary.getInt(t._1)

    val b = t._2.splitAt(BITS_SIZE)
    val bits = Binary.getInt(b._1)

    val nonce = Binary.getInt(b._2.take(NONCE_SIZE))

    BlockHeader(version, prevBlock, merkleRoot, timestamp, bits, nonce)
  }
}

