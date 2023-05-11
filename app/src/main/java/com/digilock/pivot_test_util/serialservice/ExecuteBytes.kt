package com.digilock.pivot_test_util.serialservice

class ExecuteBytes {
    var header: Byte = 0
    var cmd: Byte = 0
    var result: Byte = 0

    var ctrlbSNBytes: ByteArray = ByteArray(BOARD_UID_BYTES_SIZE)
    var lockSNBytes: ByteArray = ByteArray(BOARD_UID_BYTES_SIZE)
    var lockType: ByteArray = ByteArray(LOCK_TYPE_BYTES_SIZE)       // high byte first, low byte last
    var lockState: Byte = 0
    var lockFunc: Byte = 0
    var data: ByteArray = ByteArray(EXEC_BYTES_DATA_SIZE)

    var tail: Byte = 0
    var crcValue: Byte = 0
    var padding: Byte = 0
}