import java.io.InputStream

import scala.concurrent.Future
import akka.stream.IOResult
import akka.util.ByteString
import akka.stream.scaladsl.{ Sink, Source, StreamConverters }
import unchained.BlockHeader
import org.specs2.concurrent.ExecutionEnv
import akka.stream.contrib.TestKit.assertAllStagesStopped

import unchained.Parsing.Prefixed

class BlockHeaderSpec(implicit ee: ExecutionEnv)
  extends org.specs2.mutable.Specification {

  "BlockHeader" title

  import Commons.materializer

  "BlockHeader parser" should {
    "parse BlockHeader" >> {
      "from Block binary representation" in assertAllStagesStopped {
        val stream: InputStream = getClass.getResourceAsStream("/blocks/blk00001.dat")

        def src: Source[ByteString, Future[IOResult]] = StreamConverters.fromInputStream(() => stream)
        def parsed: Source[Prefixed[BlockHeader, ByteString], Future[IOResult]] = src.via(BlockHeader.parser)

        parsed.take(1).map { prefixed =>
          prefixed.head
        }.runWith(Sink.seq[BlockHeader]) must contain(
          exactly(Fixtures.blk00001Header)).await
      }
    }
  }
}
