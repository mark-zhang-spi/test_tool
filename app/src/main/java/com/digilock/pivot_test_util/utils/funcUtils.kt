package com.digilock.pivot_test_util.utils

import com.digilock.pivot_test_util.utils.constants.CONST.CRC8
import kotlin.experimental.xor




fun crc8ofByteArrayRange(bData: ByteArray, lowIndex: Int, highIndex: Int): Byte {
    var crcTemp: Byte = 0
    var index: Byte
    var crcIndex: Int
    for (i in lowIndex..highIndex) {
        index = (crcTemp xor bData[i])
        crcIndex = if (index < 0) {
            0x100 + index
        } else {
            index.toInt()
        }
        crcTemp = CRC8[crcIndex]
    }
    return crcTemp
}

fun xor8ofByteArrayRange(bData: ByteArray, lowIndex: Int, highIndex: Int): Byte {
    var xorTemp: Byte = 0

    for (i in lowIndex..highIndex) {
        xorTemp = xorTemp xor bData[i]
    }

    return xorTemp
}

fun getCredentailSNArray(credSN: String): ByteArray {
    val length = credSN.length / 2
    val buf = ByteArray(length)

    for(index in 0 until length) {
        val sub = credSN.substring(index * 2, index * 2 + 2)
        try {
            val data = sub.toInt(16)
            if (data > 127) {
                buf[index] = (data - 256).toByte()
            } else {
                buf[index] = data.toByte()
            }
        }catch (ex: java.lang.Exception) {

        }
    }

    return buf
}

fun covertByteArrayToString(buf: ByteArray): String {
    var credSN = ""

    for(data in buf) {
        credSN += String.format("%02X", data)
    }

    return credSN
}


fun ByteToBcd(hexByte: Byte): Byte // May need changes in future for sign issue
{
    val b: Byte
    b = (hexByte / 10).toByte()
    return (b * 16 + (hexByte - b * 10)).toByte()
}

fun IntToByte(value: Int): Byte {
    var data: Int

    if(value < 0)   data = -value
    else data = value

    data = data % 256

    if(data > 127) {
        return (-(256-data)).toByte()
    } else {
        return data.toByte()
    }
}

fun ByteToInt(value: Byte): Int {
    var data: Int

    if(value < 0)   data = (value+256)
    else data = value+0

    return data
}


