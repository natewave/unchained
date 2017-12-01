package unchained

import akka.util.ByteString

object SerializationFixtures {
  // Label: Binary representation - Expected uint16
  val variableUInt16 = Seq[(String, ByteString, BigInt)](
    ("0xFDFD00", hex2bytes("FDFD00"), BigInt(253L)),
    ("0xFD0302", hex2bytes("FD0302"), BigInt(515L)),
    ("0xFDFF7F", hex2bytes("FDFF7F"), BigInt(32767L)),
    ("0xFDFFFF", ByteString(0xFD, 0xFF, 0xFF), BigInt(65535L)))

  // Label: Binary representation - Expected uint32
  val variableUInt32 = Seq[(String, ByteString, BigInt)](
    ("0xFE00000100", hex2bytes("FE00000100"), BigInt(65536L)),
    ("0xFEFFFFFFFF", ByteString(0xFE, 0, 0, 0x01, 0), BigInt(65536L)))

  // Label: Binary representation - Expected uint64
  val variableUInt64 = Seq[(String, ByteString, BigInt)](
    ("0xFF0000000001000000",
      ByteString(0xFF, 0, 0, 0, 0, 1, 0, 0, 0),
      BigInt(4294967296L)),
    ("Long.MaxValue",
      ByteString(0xFF, -1, -1, -1, -1, -1, -1, -1, 127),
      BigInt(Long.MaxValue)))

  // ---

  @inline private def hex2bytes(hex: String): ByteString =
    ByteString(hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.
      map(Integer.parseInt(_, 16).toByte))
}
