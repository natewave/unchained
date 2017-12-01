import akka.NotUsed
import akka.util.ByteString

import akka.stream.scaladsl.{ Sink, Source }

import unchained.CompactSize, CompactSize.parser

import org.specs2.concurrent.ExecutionEnv

import akka.stream.contrib.TestKit.assertAllStagesStopped

class CompactSizeSpec(implicit ee: ExecutionEnv)
  extends org.specs2.mutable.Specification {

  "CompactSize" title

  import Commons.materializer

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
      "from 0xFDFD00 as 253" in assertAllStagesStopped {
        def src: Source[CompactSize, NotUsed] =
          Source.single(hex2bytes("FDFD00")).via(parser)

        src.runWith(Sink.seq[CompactSize]) must contain(
          exactly(CompactSize(253L))).await
      }

      "from 0xFD0302 as 515" in assertAllStagesStopped {
        def src: Source[CompactSize, NotUsed] =
          Source.single(hex2bytes("FD0302")).via(parser)

        def parsed = src.runWith(Sink.seq[CompactSize])

        parsed must contain(exactly(CompactSize(515L))).await
      }

      "from 0xFDFF7F as 32767" in assertAllStagesStopped {
        def src: Source[CompactSize, NotUsed] =
          Source.single(hex2bytes("FDFF7F")).via(parser)

        src.runWith(Sink.seq[CompactSize]) must contain(
          exactly(CompactSize(32767L))).await
      }

      "from 0xFDFFFF as 65535" in assertAllStagesStopped {
        def src: Source[CompactSize, NotUsed] =
          Source.single(ByteString(0xFD, 0xFF, 0xFF)).via(parser)

        src.runWith(Sink.seq[CompactSize]) must contain(
          exactly(CompactSize(65535L))).await
      }
    }

    "be parsed as UInt32" >> {
      "from 0xFE00000100 as 65536" in assertAllStagesStopped {
        def src: Source[CompactSize, NotUsed] =
          Source.single(hex2bytes("FE00000100")).via(parser)

        src.runWith(Sink.seq[CompactSize]) must contain(
          exactly(CompactSize(65536L))).await
      }

      "from 0xFEFFFFFFFF as 65536" in assertAllStagesStopped {
        def src: Source[CompactSize, NotUsed] =
          Source.single(ByteString(0xFE, 0, 0, 0x01, 0)).via(parser)

        src.runWith(Sink.seq[CompactSize]) must contain(
          exactly(CompactSize(65536L))).await
      }
    }

    "be parsed as UInt64" >> {
      "from 0xFF0000000001000000 as 4294967296" in assertAllStagesStopped {
        def src: Source[CompactSize, NotUsed] =
          Source.single(ByteString(0xFF, 0, 0, 0, 0, 1, 0, 0, 0)).via(parser)

        src.runWith(Sink.seq[CompactSize]) must contain(
          exactly(CompactSize(4294967296L))).await
      }

      "as Long.MaxValue" in assertAllStagesStopped {
        def src: Source[CompactSize, NotUsed] = Source.single(ByteString(
          0xFF, -1, -1, -1, -1, -1, -1, -1, 127)).via(parser)

        src.runWith(Sink.seq[CompactSize]) must contain(
          exactly(CompactSize(Long.MaxValue))).await
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
