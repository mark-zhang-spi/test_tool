package com.digilock.pivot_test_util.serialservice

class LockerInfo {
    var type: Int = 0
    var name = ""
    var credSNBytes: ByteArray = ByteArray(MAX_CREDENTIAL_BYTES_SIZE)
    var available: Byte = LOCK_IS_AVAILABLE
    val ctrlbUidBytes: ByteArray = ByteArray(BOARD_UID_BYTES_SIZE)
    val lockUidBytes: ByteArray = ByteArray(BOARD_UID_BYTES_SIZE)
}