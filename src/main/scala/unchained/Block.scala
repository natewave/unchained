package unchained

/*

Protocol specification:
  - https://en.bitcoin.it/wiki/Protocol_documentation#block


*/

final case class BlockHeader(
  version: Int,
  prevBlock: Hash,  // 	The hash value of the previous block this particular block references
  merkleRoot: Hash, // 	The reference to a Merkle tree collection which is a hash of all transactions related to this block
  timestamp: Long,  //	A Unix timestamp recording when this block was created (Currently limited to dates before the year 2106!)
  bits: Long,       // The calculated difficulty target being used for this block
  nonce: Long       //	The nonce used to generate this block… to a
)

final case class Hash(value: String) extends AnyVal

object Block {
  // def apply(input: InputStream): Try[Block] = ???
  // def apply(bytes: ???): ??? = ???
}
