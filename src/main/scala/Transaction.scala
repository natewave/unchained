package io.github.natewave

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
class Transaction(
  val version: Int,
  val inputSize: Transaction.CompactSize,
  val outputSize: Transaction.CompactSize,
  val inputs: Source[Transaction.Input, NotUsed],
  val outputs: Source[Transaction.Outpoint, NotUsed],
  val lockTime: LockTime)

object Transaction {
  /**
   * Transaction input ([[https://bitcoin.org/en/developer-reference#txin TxIn]]).
   *
   * @param previousOutput the previous outpoint being spent
   * @param signatureScript the signature script
   * @param sequence the [[https://bitcoin.org/en/glossary/sequence-number sequence number]]
   */
  case class Input(
    previousOutput: Outpoint,
    signatureScript: Int,
    sequence: Int) // unsigned int 32

  /**
   * [[https://bitcoin.org/en/developer-reference#compactsize-unsigned-integers Compact size]]
   */
  class CompactSize(val value: Long) extends AnyVal

  /**
   * [[https://bitcoin.org/en/glossary/signature-script Signature script]]
   *
   * @param value the underlying script value
   */
  class SignatureScript(val value: String) extends AnyVal

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
