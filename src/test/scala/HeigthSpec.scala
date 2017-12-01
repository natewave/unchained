import akka.NotUsed
import akka.util.ByteString

import akka.stream.scaladsl.{ Sink, Source }

import unchained.Height, Height.parser

import org.specs2.concurrent.ExecutionEnv
import org.specs2.specification.core.Fragments

import akka.stream.contrib.TestKit.assertAllStagesStopped

class HeightSpec(implicit ee: ExecutionEnv)
  extends org.specs2.mutable.Specification {

  "Height" title

  import unchained.{ Commons, SerializationFixtures }, Commons.materializer

  "Binary representation" should {
    "be parsed from 0x0103 (1byte) as 3" in assertAllStagesStopped {
      def src: Source[Height, NotUsed] =
        Source.single(ByteString(1, 3)).via(parser)

      src.runWith(Sink.seq[Height]) must contain(exactly(Height(3))).await
    }

    "be parsed as UInt16" >> {
      val prefix = ByteString(0x02) // 16bits

      Fragments.foreach(SerializationFixtures.variableUInt16) {
        case (label, binary, expected) =>
          s"from $label (2bytes) as $expected" in assertAllStagesStopped {
            def src: Source[Height, NotUsed] =
              Source.single(prefix ++ binary).via(parser)

            src.runWith(Sink.seq[Height]) must contain(
              exactly(Height(expected))).await
          }
      }
    }

    "be parsed from 0x034E0105 (3bytes) as 328014" in assertAllStagesStopped {
      def src: Source[Height, NotUsed] =
        Source.single(ByteString(3, 78, 1, 5)).via(parser)

      src.runWith(Sink.seq[Height]) must contain(
        exactly(Height(328014))).await
    }

    "be parsed as UInt32" >> {
      val prefix = ByteString(0x04) // 32bits

      Fragments.foreach(SerializationFixtures.variableUInt32) {
        case (label, binary, expected) =>
          s"from $label (4bytes) as $expected" in assertAllStagesStopped {
            def src: Source[Height, NotUsed] =
              Source.single(prefix ++ binary).via(parser)

            src.runWith(Sink.seq[Height]) must contain(
              exactly(Height(expected))).await
          }
      }
    }
  }
}
