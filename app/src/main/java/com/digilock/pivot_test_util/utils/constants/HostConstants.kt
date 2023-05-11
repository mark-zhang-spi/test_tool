package com.digilock.pivot_test_util.utils.constants

import java.time.format.DateTimeFormatter


const val PACKET_IS_BROKEN = "Received Packet is broken"
const val PACKET_CRC_CHECK_FAILED = "Received Packet CRC check failed"
const val PACKET_MISS_HEADER = "Received Packet missed header"
const val PACKET_MISS_TAIL = "Received Packet missed tail"

const val UPGRADE_DIR_NAME = "/digilock/sidemount/upgrade"
const val TESTING_DIR_NAME = "/digilock/Testing"

const val CTRLB_FIRMWARE = 0x0020
const val WM_LOCK_M_FIRMWARE = 0x273B
const val WM_LOCK_L_FIRMWARE = 0x4E4B
const val WLRFRDM_LOCK_M_FIRMWARE = 0x2739
const val WLRFRDM_LOCK_L_FIRMWARE = 0x4E49

const val OFFSET_LOCK_VERSION: Byte = 0x00
const val LOCK_VERSION_SIZE: Byte = 0x03

const val OFFSET_TEST_STEP: Int = 0x03
const val OFFSET_TEST_RESULT: Int = 0x04

const val TEST_STEP_CHIPS: Byte = 0x01          // Primary components, HID, BMD, TOUCHPAD, etc
const val TEST_STEP_RED_LED: Byte = 0x02        // RED LED blink
const val TEST_STEP_GREEN_LED: Byte = 0x03      // GREEN LED blink
const val TEST_STEP_SENSORS: Byte = 0x04        // Sensor
const val TEST_STEP_RFID: Byte = 0x05           // RFID reader
const val TEST_STEP_EEPROM: Byte = 0x06         // External EEPROM read/write.
const val TEST_STEP_COMPLETE: Byte = 0x07       // Testing complete
const val TEST_BOARD_INFO: Byte = 0x08          // Testing board information
const val TEST_CTRLB_COM: Byte = 0x09           // Testing control board COM ports
const val TEST_STEP_SRAM: Byte = 0x0A           // External RAM read/write.
const val TEST_MOBILE_ACCESS: Byte = 0x0B       // Mobile ID access

const val IDX_HID_CHIP: Byte = 0x01
const val IDX_BMD_CHIP: Byte = 0x02
const val IDX_TOUCH_PAD: Byte = 0x03
const val IDX_MOTOR_SENSOR: Byte = 0x04
const val IDX_LATCH_SENSOR: Byte = 0x05
const val IDX_DOOR_SENSOR: Byte = 0x06
const val IDX_TIMER_TICK: Byte = 0x07
const val IDX_TEST_RESULT: Byte = 0x08
const val IDX_TIMER_PERIOD: Byte = 0x09
const val IDX_MCU_TYPE: Byte = 0x0A
const val IDX_LEGIC_CHIP: Byte = 0x0B

const val RESULT_PASS: Byte = 0x01
const val RESULT_FAIL: Byte = 0x00

const val COMPONENT_READY: Byte = 0x01

const val CTRLB_MCU128: Byte = 0x55
const val CTRLB_MCU256: Byte = -0x34

val MAP_UPGRADE_FW_NAME = mapOf(
    CTRLB_FIRMWARE to "ctrlb_upgrade",
    WM_LOCK_M_FIRMWARE to "wm_m_upgrade",
    WM_LOCK_L_FIRMWARE to "wm_l_upgrade",
    WLRFRDM_LOCK_M_FIRMWARE to "wlrfrdm_m_upgrade",
    WLRFRDM_LOCK_L_FIRMWARE to "wlrfrdm_l_upgrade"
)

val MAP_UPGRADE_DEVICE_NAME = mapOf(
    CTRLB_FIRMWARE to "Control board",
    WM_LOCK_M_FIRMWARE to "wm_m lock",
    WM_LOCK_L_FIRMWARE to "wm_l lock",
    WLRFRDM_LOCK_M_FIRMWARE to "wlrfrdm_m lock",
    WLRFRDM_LOCK_L_FIRMWARE to "wlrfrdm_l lock"
)

val MAP_LED_NAME = mapOf(
    TEST_STEP_RED_LED to "\nRed led",
    TEST_STEP_GREEN_LED to "Green led"
)

val MAP_TIMER_PERIOD_FRONT = mapOf(
    TEST_STEP_RED_LED to "\nRed led will blink ",
    TEST_STEP_GREEN_LED to "\nGreen led will blink ",
    TEST_STEP_SENSORS to "\nClose door in ",
    TEST_STEP_RFID to "\nPresent a card in ",
    TEST_CTRLB_COM to "\nConnect cable between COM2 and COM5 in "
)

val MAP_TIMER_PERIOD_BACK = mapOf(
    TEST_STEP_RED_LED to "",
    TEST_STEP_GREEN_LED to "",
    TEST_STEP_SENSORS to "to test sensors. ",
    TEST_STEP_RFID to " to test RFID.",
    TEST_CTRLB_COM to "to test COM ports."
)

val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

const val DOWNLOAD_DIR_NAME = "/digilock/pivot/download"
const val TESTING_LOG_FILE_NAME = "/pivot_test_log.txt"
const val NEW_LINE = "\r\n"

const val markedTick = "\u2713"
const val markedCross = "\u2715"
const val markedMiss = "\u2735"

const val SENSOR_IS_FAIL: Byte = 0x00
const val SENSOR_IS_OK: Byte = 0x01
const val SENSOR_IS_MISS: Byte = 0x02

val MAP_SENSOR_STATE = mapOf(
    SENSOR_IS_FAIL to "sensor is FAIL ",
    SENSOR_IS_OK to "sensor is OK ",
    SENSOR_IS_MISS to "sensor is MISS "
)

val MAP_TEST_LABEL = mapOf(
    TEST_STEP_EEPROM to "\nTest EEPROM:\n",
    TEST_STEP_SRAM to "\nTest SRAM:\n"
)