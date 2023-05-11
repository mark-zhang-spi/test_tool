package com.digilock.pivot_test_util.serialservice

class ExecuteBytes_BL {
    var header: Byte = 0
    var cmd: Byte = 0
    var dataSize: Byte = 0

    var data: ByteArray = ByteArray(EXEC_BYTES_DATA_SIZE_BL)

    var tail: Byte = 0
    var crcValue: Byte = 0
    var padding: Byte = 0
}