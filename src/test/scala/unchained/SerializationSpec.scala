package unchained

import akka.NotUsed
import akka.util.ByteString

import akka.stream.scaladsl.{ Sink, Source }

import org.specs2.concurrent.ExecutionEnv
import org.specs2.specification.core.Fragments

import akka.stream.contrib.TestKit.assertAllStagesStopped

class SerializationSpecSpec(implicit ee: ExecutionEnv)
  extends org.specs2.mutable.Specification {

  "Serialization" title

  import Commons.materializer

  "Binary representation" should {
    "be parsed as UInt16" >> {
      val byteCount: Byte = 2 // 16bits

      Fragments.foreach(SerializationFixtures.variableUInt16) {
        case (label, binary, expected) =>
          s"from $label as $expected" in assertAllStagesStopped {
            def src: Source[BigInt, NotUsed] =
              Serialization.variableUnsigned(byteCount, binary)

            src.runWith(Sink.seq[BigInt]) must contain(exactly(expected)).await
          }
      }
    }

    "be parsed as UInt32" >> {
      val byteCount: Byte = 4 // 32bits

      Fragments.foreach(SerializationFixtures.variableUInt32) {
        case (label, binary, expected) =>
          s"from $label as $expected" in assertAllStagesStopped {
            def src: Source[BigInt, NotUsed] =
              Serialization.variableUnsigned(byteCount, binary)

            src.runWith(Sink.seq[BigInt]) must contain(exactly(expected)).await
          }
      }
    }

    "be parsed as UInt64" >> {
      val byteCount: Byte = 8 // 64bits

      Fragments.foreach(SerializationFixtures.variableUInt64) {
        case (label, binary, expected) =>
          s"from $label as $expected" in assertAllStagesStopped {
            def src: Source[BigInt, NotUsed] =
              Serialization.variableUnsigned(byteCount, binary)

            src.runWith(Sink.seq[BigInt]) must contain(exactly(expected)).await
          }
      }

      "from 0xFFFFFFFFFFFFFFFF as (2 * Long.MaxValue)" in {
        assertAllStagesStopped {
          val bytes = Array[Byte](
            0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte,
            0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)

          def src: Source[BigInt, NotUsed] =
            Serialization.variableUnsigned(byteCount, ByteString(bytes))

          src.runWith(Sink.seq[BigInt]).
            map(_.map(_ / 2 toLong)) must contain(exactly(Long.MaxValue)).await
        }
      }
    }
  }
}
