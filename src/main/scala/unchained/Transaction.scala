package unchained

import akka.NotUsed
import akka.stream.scaladsl.Source

/**
 * @param value either a block number/height (if <500M) or an unix timestamp
 */
class LockTime(val value: Int) extends AnyVal

/**
 * @param version [[https://bitcoin.org/en/developer-guide#term-transaction-version-number the version number]] (currently 1)
 * @param inputSize the (expected) number of [[inputs]]
 * @param outputSize the (expected) number of [[outputs]]
 * @param inputs the [[Transaction.Input]]s
 * @param outputs the [[Transaction.Outpoint]]s
 */
class PlainTransaction(
  val version: Int,
  val inputSize: CompactSize,
  val outputSize: CompactSize,
  private[unchained] val inputs: Source[Transaction.Input, NotUsed],
  private[unchained] val outputs: Source[Transaction.Output, NotUsed],
  val lockTime: LockTime)

object Transaction {
  /**
   * Transaction input ([[https://bitcoin.org/en/developer-reference#txin TxIn]]).
   *
   * @param previousOutput the previous outpoint being spent
   * @param scriptBytes the size of the signature script
   * @param sequence the [[https://bitcoin.org/en/glossary/sequence-number sequence number]]
   */
  case class Input(
    previousOutput: Outpoint,
    scriptBytes: CompactSize,
    signatureScript: String,
    sequence: Int) // unsigned int 32

  /**
   * Transaction output ([[https://bitcoin.org/en/developer-reference#txout TxOut]])
   *
   * @param value the number of satoshis to spend
   * @param pkScriptBytes the number of bytes in the pubkey script
   * @param pkScript the conditions which must be satisfied to spend this output
   */
  case class Output(
    value: Long,
    pkScriptBytes: CompactSize,
    pkScript: String)

  /**
   * Transaction [[https://bitcoin.org/en/developer-reference#outpoint outpoint]]
   *
   * @param hash the TXID of the transaction holding the output to spend
   * @param index the output index number of the specific output to spend from the transaction (the first one is `0x00000000`)
   */
  case class Outpoint(
    hash: String, // char[32]
    index: Int) // unsigned int 32

}
