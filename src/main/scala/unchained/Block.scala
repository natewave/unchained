package unchained

//case class Block(header, coinbase, tx[])

object Block {
  val HEADER_SIZE = 80 // header size represented in bytes

  // data to be skipped at the begining of a block
  val MAGIC_NO_SIZE = 4
  val BLOCK_SIZE_SIZE = 4 // size of block_size (4 bytes)
  val SKIP_SIZE = MAGIC_NO_SIZE + BLOCK_SIZE_SIZE

  val METADATA_SIZE = SKIP_SIZE + HEADER_SIZE

  def headerWithPayload = BlockHeader.parser _
}