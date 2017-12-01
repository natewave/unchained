import akka.NotUsed
import akka.util.ByteString

import akka.stream.scaladsl.{ Sink, Source }

import unchained.CompactSize, CompactSize.parser

import org.specs2.concurrent.ExecutionEnv
import org.specs2.specification.core.Fragments

import akka.stream.contrib.TestKit.assertAllStagesStopped

class CompactSizeSpec(implicit ee: ExecutionEnv)
  extends org.specs2.mutable.Specification {

  "CompactSize" title

  import Commons.materializer
  import unchained.SerializationFixtures

  "Binary representation" should {
    "be parsed as UInt8" >> {
      "from 0" in assertAllStagesStopped {
        def src: Source[CompactSize, NotUsed] =
          Source.single(ByteString(Array[Byte](0))).via(parser)

        src.runWith(Sink.seq[CompactSize]) must contain(
          exactly(CompactSize(0L))).await
      }

      "from 127" in assertAllStagesStopped {
        def src: Source[CompactSize, NotUsed] =
          Source.single(ByteString(Array(127.toByte))).via(parser)

        src.runWith(Sink.seq[CompactSize]) must contain(
          exactly(CompactSize(127L))).await
      }

      "from 128" in assertAllStagesStopped {
        def src: Source[CompactSize, NotUsed] =
          Source.single(ByteString(Array(128.toByte))).via(parser)

        src.runWith(Sink.seq[CompactSize]) must contain(
          exactly(CompactSize(128L))).await
      }

      "from 252" in assertAllStagesStopped {
        def src: Source[CompactSize, NotUsed] =
          Source.single(ByteString(Array(252.toByte))).via(parser)

        src.runWith(Sink.seq[CompactSize]) must contain(
          exactly(CompactSize(252L))).await
      }
    }

    "be parsed as UInt16" >> {
      Fragments.foreach(SerializationFixtures.variableUInt16) {
        case (label, binary, expected) =>
          s"from $label as $expected" in assertAllStagesStopped {
            def src: Source[CompactSize, NotUsed] =
              Source.single(binary).via(parser)

            src.runWith(Sink.seq[CompactSize]) must contain(
              exactly(CompactSize(expected))).await
          }
      }
    }

    "be parsed as UInt32" >> {
      Fragments.foreach(SerializationFixtures.variableUInt32) {
        case (label, binary, expected) =>
          s"from $label as $expected" in assertAllStagesStopped {
            def src: Source[CompactSize, NotUsed] =
              Source.single(binary).via(parser)

            src.runWith(Sink.seq[CompactSize]) must contain(
              exactly(CompactSize(expected))).await
          }
      }
    }

    "be parsed as UInt64" >> {
      Fragments.foreach(SerializationFixtures.variableUInt64) {
        case (label, binary, expected) =>
          s"from $label as $expected" in assertAllStagesStopped {
            def src: Source[CompactSize, NotUsed] =
              Source.single(binary).via(parser)

            src.runWith(Sink.seq[CompactSize]) must contain(
              exactly(CompactSize(expected))).await
          }
      }

      "from 0xFFFFFFFFFFFFFFFFFF as (2 * Long.MaxValue)" in {
        assertAllStagesStopped {
          val bytes = Array[Byte](
            0xFF.toByte,
            0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte,
            0xFF.toByte, 0xFF.toByte, 0xFF.toByte, 0xFF.toByte)

          def src: Source[CompactSize, NotUsed] =
            Source.single(ByteString(bytes)).via(parser)

          src.runWith(Sink.seq[CompactSize]).
            map(_.map(i => (i.value / 2).toLong)) must contain(
              exactly(Long.MaxValue)).await
        }
      }
    }
  }

  // ---

  @inline def hex2bytes(hex: String): ByteString =
    ByteString(hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.
      map(Integer.parseInt(_, 16).toByte))
}
