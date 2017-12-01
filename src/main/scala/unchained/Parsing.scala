package unchained

import akka.stream.scaladsl.{ Flow, Source }
import akka.util.ByteString

object Parsing {

  def bytesPrefixAndTail(exactly: Int, acc: ByteString = ByteString.empty): Flow[ByteString, Prefixed[ByteString, ByteString], _] = {
    Flow[ByteString].prefixAndTail(1).flatMapConcat {
      case (prefix: Seq[ByteString], tail: Source[ByteString, _]) =>
        val bytes: ByteString = acc ++ prefix.flatten

        // chunk is larger than needed bytes
        // adjust tail by prepending the extra bytes into the original source
        if (bytes.length >= exactly) {
          val head = bytes.take(exactly)
          val remaining = bytes.drop(exactly)
          val newSource = Source.single(remaining) ++ tail
          val prefixed = Prefixed[ByteString, ByteString](head, newSource)

          Source.single(prefixed)
        } else tail.via(bytesPrefixAndTail(exactly, bytes))

      case e => Source.failed(new Exception(s"[-] Error parsing block: Could not get block header: $e"))
    }
  }

  case class Prefixed[A, B](head: A, tail: Source[B, _])

  object Binary {
    import java.nio.ByteOrder

    def getInt(bytes: ByteString) = bytes.asByteBuffer.order(ByteOrder.LITTLE_ENDIAN).getInt
  }

  case class Hash(val value: String) extends AnyVal

  object Hash {
    def apply(bytes: Array[Byte]): Hash = {
      val hash = bytes.reverse.map(b => "%02x".format(b)).mkString
      Hash(hash)
    }
  }
}

