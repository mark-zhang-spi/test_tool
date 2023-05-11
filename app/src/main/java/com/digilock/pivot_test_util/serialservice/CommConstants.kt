package com.digilock.pivot_test_util.serialservice

const val RX_TX_BUFFER_SIZE: Int = 1024

// Regular communication packet
const val HOST_COMMAND_SIZE: Int = 32
const val COMM_HEADER_SIZE: Int = 8
const val HOST_PACKET_SIZE: Int = COMM_HEADER_SIZE + HOST_COMMAND_SIZE

const val LOCK_TYPE_BYTES_SIZE: Int = 2
const val BOARD_UID_BYTES_SIZE: Int = 4
const val MAX_CREDENTIAL_BYTES_SIZE: Int = 8
const val EXEC_BYTES_DATA_SIZE: Int = 14

const val T_BOLT: Byte = 0x55
const val P_BOLT: Byte = 0x58

const val LOCK_IS_AVAILABLE: Byte = 0x00
const val LOCK_IS_USE: Byte = 0x01
const val LOCK_IS_NOT_FOUND: Byte = 0x02


// Upgrade communication packet
const val MINIMUM_WRITE_BLOCK_SIZE: Int = 8
const val HOST_COMMAND_SIZE_BL: Int = 122
const val HOST_PACKET_SIZE_BL: Int = COMM_HEADER_SIZE + HOST_COMMAND_SIZE_BL
const val EXEC_BYTES_DATA_SIZE_BL: Int = 116

const val HEADER_DATA: Byte = 0x55
const val RESP_PKT_HEADER: Byte = 0x7E

const val HOST_PACKET_HEADER: Byte = 0x7E
const val HOST_PACKET_TAIL: Byte = -0x56

const val EX_LOCKER_UNLOCKED: Byte = 0x32
const val EX_LOCKER_LOCKED: Byte = 0x31

const val EX_CTRLB_IS_STARTED: Byte = 0x50
const val EX_CTRLB_IS_INITIALIZED: Byte = 0x51
const val EX_CTRLB_IS_MOUNTED: Byte = 0x52

const val CMD_HOST_FIND_CTRLB: Byte = 0x3E

const val CMD_CTRLB_FIND_HOST: Byte = 0x3D
const val CMD_FIND_CTRLB: Byte = 0x3E

const val CMD_INITIALIZE_PANEL: Byte = 0x40
const val CMD_PROGRAM_NEXT_LOCK: Byte = 0x52

const val CMD_ACCESS_CREDENTIAL: Byte = 0x21
const val CMD_LOCK_STATUS: Byte = 0x23

const val CMD_MOUNT_DONE: Byte = 0x4A

const val CMD_SELF_TEST_CTRLB: Byte = 0x60
const val CMD_SELF_TEST_LOCK: Byte = 0x61

const val CMD_HOST_UPGRADE_INIT: Byte = 0x70
const val CMD_HOST_BT_TRANSFER_DATA: Byte = 0x71
const val CMD_HOST_BT_TRANSFER_COMPLETE: Byte = 0x72
const val CMD_HOST_UPGRADE_COMPLETE: Byte = 0x73

const val CMD_WRITE_BOARD_UID:Byte = 0x41
const val CMD_READ_PANEL_INFO: Byte = 0x42
const val CMD_LOCK_DOOR: Byte = 0x43
const val CMD_OPEN_DOOR: Byte = 0x44
const val CMD_READ_LOCKER_STATUS: Byte = 0x46
const val CMD_REMOVE_LOCKER: Byte = 0x47
const val CMD_PROGRAM_PANEL_BOARD: Byte = 0x4D
const val CMD_UNMOUNT_PANEL: Byte = 0x4F

const val HOST_UPLOAD: Byte = 0x00
const val HOST_EXEC_RESULT_PASS: Byte = 0x01
const val HOST_EXEC_RESULT_FAIL: Byte = 0x02
const val HOST_EXEC_PARAM_ERR: Byte = 0x03
const val HOST_EXEC_BOARD_ERROR: Byte = 0x04
const val HOST_EXEC_UPSUPPORTED_ERROR: Byte = 0x05
const val HOST_EXEC_BOARD_TIMEOUT: Byte = 0x10
const val HOST_EXEC_RESP_COLLISION: Byte = 0x20
const val HOST_EXEC_UID_COLLISION: Byte = 0x40

const val LOCK_VER_OFFSET: Byte = 0x00
const val CTRLB_VER_OFFSET: Byte = 0x03

val MAP_COMMAND_NAME = mapOf(
    CMD_CTRLB_FIND_HOST to "Control Board find HOST ",
    CMD_INITIALIZE_PANEL to "Initialize Lock ",
    CMD_SELF_TEST_CTRLB to "Test Control Board ",
    CMD_SELF_TEST_LOCK to "Test Lock "
)