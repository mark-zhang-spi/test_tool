package com.digilock.pivot_test_util.main

import android.content.Context
import com.digilock.pivot_test_util.serialservice.*
import com.digilock.pivot_test_util.utils.ByteToInt
import com.digilock.pivot_test_util.utils.constants.*
import com.digilock.pivot_test_util.utils.covertByteArrayToString
import com.digilock.pivot_test_util.utils.crc8ofByteArrayRange
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.*
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext


class Presenter(private val view : Contract.View,
                private val source : DataSource) :
    Contract.Presenter,
    CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val compositeDisposable = CompositeDisposable()

    private val usbRxPacket = HostCommPacket()
    private val usbTxPacket = HostCommPacket()
    private val usbTxPacket_bl = HostCommPacket_BL()

    private var uid: ByteArray = ByteArray(BOARD_UID_BYTES_SIZE)
    private var uidString = ""

    private lateinit var mContext: Context

    init {
        view.setPresenter(this)
    }

    override fun start() {
        compositeDisposable.clear()
    }

    override fun connectDevice() {
    }

    override fun findCtrlb() {
        usbTxPacket.cleanPacketBuffer()
        usbTxPacket.executeBytes.header = HOST_PACKET_HEADER
        usbTxPacket.executeBytes.cmd = CMD_FIND_CTRLB
        usbTxPacket.executeBytes.tail = HOST_PACKET_TAIL

        usbTxPacket.fromExecuteBytesToBuffer()
        usbTxPacket.packetBuffer[usbTxPacket.packetSize - 2] = crc8ofByteArrayRange(usbTxPacket.packetBuffer, COMM_HEADER_SIZE, usbTxPacket.packetSize - 3)

        view.sendUsbCommand(usbTxPacket.packetBuffer)
    }


    override fun initializeLock() {
        usbTxPacket.cleanPacketBuffer()
        usbTxPacket.executeBytes.header = HOST_PACKET_HEADER
        usbTxPacket.executeBytes.cmd = CMD_INITIALIZE_PANEL
        usbTxPacket.executeBytes.tail = HOST_PACKET_TAIL

        usbTxPacket.fromExecuteBytesToBuffer()
        usbTxPacket.packetBuffer[usbTxPacket.packetSize - 2] = crc8ofByteArrayRange(usbTxPacket.packetBuffer, COMM_HEADER_SIZE, usbTxPacket.packetSize - 3)

        view.sendUsbCommand(usbTxPacket.packetBuffer)
    }

    override fun initTestingLog(context: Context, append: Boolean) {
        mContext = context
        source.initTestingLog(context, append)
    }

    override fun saveTestLog(logIndex: Int) {
        if(source.saveTestLog(logIndex)) {
            source.initTestingLog(mContext, false)

            view.showMessage("Save current test log file succeed.", false)
        } else {
            view.showMessage("Save current test log file failed.", true)
        }
    }

    override fun testCtrlb() {
        usbTxPacket.cleanPacketBuffer()
        usbTxPacket.executeBytes.header = HOST_PACKET_HEADER
        usbTxPacket.executeBytes.cmd = CMD_SELF_TEST_CTRLB
        usbTxPacket.executeBytes.tail = HOST_PACKET_TAIL

        usbTxPacket.fromExecuteBytesToBuffer()
        usbTxPacket.packetBuffer[usbTxPacket.packetSize - 2] = crc8ofByteArrayRange(usbTxPacket.packetBuffer, COMM_HEADER_SIZE, usbTxPacket.packetSize - 3)

        view.sendUsbCommand(usbTxPacket.packetBuffer)

        source.dumpTestingLogLine(mContext, "Test control board @ ${LocalDateTime.now().format(formatter)}\n\n")
    }

    override fun testLock() {
        usbTxPacket.cleanPacketBuffer()
        usbTxPacket.executeBytes.header = HOST_PACKET_HEADER
        usbTxPacket.executeBytes.cmd = CMD_SELF_TEST_LOCK
        usbTxPacket.executeBytes.tail = HOST_PACKET_TAIL

        usbTxPacket.fromExecuteBytesToBuffer()
        usbTxPacket.packetBuffer[usbTxPacket.packetSize - 2] = crc8ofByteArrayRange(usbTxPacket.packetBuffer, COMM_HEADER_SIZE, usbTxPacket.packetSize - 3)

        view.sendUsbCommand(usbTxPacket.packetBuffer)

        source.dumpTestingLogLine(mContext, "Test lock @ ${LocalDateTime.now().format(formatter)}\n\n")
    }


    override fun processUSBPacket(data: String) {
        if(data.length != (HOST_PACKET_SIZE- COMM_HEADER_SIZE) * 3) {
           view.showMessage(PACKET_IS_BROKEN, true)
            return
        }

        // Decode USB data
        usbRxPacket.cleanPacketBuffer()
        for(index in 0 until HOST_PACKET_SIZE-COMM_HEADER_SIZE) {
            val sub = data.substring(index * 3, index * 3 + 2)
            val intVal = Integer.parseInt(sub, 16)
            if (intVal > 127)
                usbRxPacket.packetBuffer[index+COMM_HEADER_SIZE] = (intVal - 0x100).toByte()
            else
                usbRxPacket.packetBuffer[index+COMM_HEADER_SIZE] = intVal.toByte()
        }

        usbRxPacket.fromBufferToExecuteBytes()
        if(usbRxPacket.executeBytes.crcValue != crc8ofByteArrayRange(usbRxPacket.packetBuffer, COMM_HEADER_SIZE, usbRxPacket.packetSize - 3)) {
            val crcVal = crc8ofByteArrayRange(usbRxPacket.packetBuffer, COMM_HEADER_SIZE, usbRxPacket.packetSize - 3)
            view.showMessage(PACKET_CRC_CHECK_FAILED + "CRC: ${usbRxPacket.executeBytes.crcValue}, crcVal: ${crcVal}", true)
        } else {
            if(usbRxPacket.executeBytes.header != HOST_PACKET_HEADER) {
                view.showMessage(PACKET_MISS_HEADER, true)
            } else {
                if(usbRxPacket.executeBytes.tail != HOST_PACKET_TAIL) {
                    view.showMessage(PACKET_MISS_TAIL, true)
                } else {
                    execUSBPacket(usbRxPacket)
                }
            }
        }
    }

    private fun execUSBPacket(rxPacket: HostCommPacket) {
        if(!MAP_COMMAND_NAME.containsKey(rxPacket.executeBytes.cmd)) {
            view.showMessage("Unknown command.", true)
            return
        }

        when(rxPacket.executeBytes.result) {
            HOST_UPLOAD -> {
                when(rxPacket.executeBytes.cmd) {
                    CMD_CTRLB_FIND_HOST -> {
                        val version = "v.${usbRxPacket.executeBytes.data[CTRLB_VER_OFFSET+0]}.${usbRxPacket.executeBytes.data[CTRLB_VER_OFFSET+1]}.${usbRxPacket.executeBytes.data[CTRLB_VER_OFFSET+2]}"

                        usbTxPacket.cleanPacketBuffer()
                        usbTxPacket.executeBytes.result = HOST_EXEC_RESULT_PASS
                        usbTxPacket.executeBytes.header = HOST_PACKET_HEADER
                        usbTxPacket.executeBytes.cmd = CMD_CTRLB_FIND_HOST
                        usbTxPacket.executeBytes.tail = HOST_PACKET_TAIL

                        usbTxPacket.fromExecuteBytesToBuffer()
                        usbTxPacket.packetBuffer[usbTxPacket.packetSize - 2] = crc8ofByteArrayRange(usbTxPacket.packetBuffer, COMM_HEADER_SIZE, usbTxPacket.packetSize - 3)

                        view.sendUsbCommand(usbTxPacket.packetBuffer)
                        view.showMessage(" Control board find HOST successfully. ${version}", false)
                    }

                    CMD_SELF_TEST_CTRLB -> {
                        val testStep = usbRxPacket.executeBytes.data[OFFSET_TEST_STEP]
                        when(testStep) {
                            TEST_STEP_EEPROM -> {
                                processTestWithTimer(testStep, "Unexpected report type in EEPROM testing report.", false)
                            }

                            TEST_STEP_SRAM -> {
                                processTestWithTimer(testStep, "Unexpected report type in SRAM testing report.", false)
                            }

                            TEST_CTRLB_COM -> {
                                processTestWithTimer(testStep, "Unexpected report type in COMs testing report.", true)
                            }
                        }
                    }

                    CMD_SELF_TEST_LOCK -> {
                        val testStep = usbRxPacket.executeBytes.data[OFFSET_TEST_STEP]
                        when(testStep) {
                            TEST_STEP_RED_LED, TEST_STEP_GREEN_LED -> {
                                processTestWithTimer(testStep, "Unexpected report type in ${MAP_LED_NAME[testStep]} led testing.", false)
                            }

                            TEST_STEP_SENSORS -> {
                                processTestWithTimer(testStep,"Unexpected report type in SENSOR testing report.", true)
                            }

                            TEST_STEP_RFID -> {
                                processTestWithTimer(testStep, "Unexpected report type in RFID testing report.", true)
                            }

                            TEST_MOBILE_ACCESS -> {
                                processTestWithTimer(testStep, "Unexpected report type in Mobile access testing report.", true)
                            }

                            TEST_STEP_EEPROM -> {
                                processTestWithTimer(testStep, "Unexpected report type in EEPROM testing report.", false)
                            }

                            else -> {
                                view.showMessage("Unsupported report type", true)
                            }
                        }
                    }

                    else -> {
                        view.showMessage("Unsupported command", true)
                    }
                }
            }

            HOST_EXEC_RESULT_PASS -> {
                when(rxPacket.executeBytes.cmd) {
                    CMD_INITIALIZE_PANEL -> {
                        view.showMessage(" Initialize Panel: succeed.", false)
                    }

                    CMD_SELF_TEST_CTRLB -> {
                        val deviceType = (ByteToInt(usbRxPacket.executeBytes.lockType[0]) shl(8)) + ByteToInt(usbRxPacket.executeBytes.lockType[1])
                        val testStep = usbRxPacket.executeBytes.data[OFFSET_TEST_STEP]
                        when(testStep) {
                            TEST_BOARD_INFO -> {
                                processReport(deviceType, TEST_BOARD_INFO)
                            }

                            TEST_STEP_EEPROM -> {
                                processSingleTestingResult(testStep, "  EEPROM: OK. \n", "  EEPROM: FAIL. \n")
                            }

                            TEST_STEP_SRAM -> {
                                processSingleTestingResult(testStep, "  SRAM: OK. \n", "  SRAM: FAIL. \n")
                            }

                            TEST_CTRLB_COM -> {
                                processSingleTestingResult(testStep, "  COM ports: OK \n", "  COM ports: FAIL \n")
                            }

                            TEST_STEP_COMPLETE -> {
                                processSingleTestingResult(TEST_STEP_COMPLETE,"  Test Complete: OK \n\n", "  Test Complete: FAIL \n\n")
                            }

                            else -> {
                                view.showMessage("  Unsupported testing step.", true)
                            }
                        }
                    }

                    CMD_SELF_TEST_LOCK -> {
                        val deviceType = (ByteToInt(usbRxPacket.executeBytes.lockType[0]) shl(8)) + ByteToInt(usbRxPacket.executeBytes.lockType[1])
                        when(deviceType) {
                            WLRFRDM_LOCK_M_FIRMWARE, WLRFRDM_LOCK_L_FIRMWARE -> {
                                val testStep = usbRxPacket.executeBytes.data[OFFSET_TEST_STEP]
                                when(testStep) {
                                    TEST_BOARD_INFO -> {
                                        processReport(deviceType, TEST_BOARD_INFO)
                                    }

                                    TEST_STEP_RED_LED -> {
                                        source.dumpTestingLogLine(mContext, "\n")
                                    }

                                    TEST_STEP_GREEN_LED -> {
                                        source.dumpTestingLogLine(mContext, "\n")
                                    }

                                    TEST_STEP_CHIPS -> {
                                        var hidState = false
                                        var bmd_legic_State = false
                                        var touchPadState = false
                                        var hidVer = ""
                                        var bmd_legic_Ver = ""
                                        var useLEGIC = false

                                        source.dumpTestingLogLine(mContext, "\nPrimary chip state:\n")

                                        var offset = OFFSET_TEST_RESULT
                                        var rptCompCount = usbRxPacket.executeBytes.data[offset++]
                                        while(rptCompCount-- > 0) {
                                            when(usbRxPacket.executeBytes.data[offset++]) {
                                                IDX_HID_CHIP -> {
                                                    val hiVerByte = usbRxPacket.executeBytes.data[offset++]
                                                    val loVerByte = usbRxPacket.executeBytes.data[offset++]

                                                    if((hiVerByte!=0.toByte()) || (loVerByte!=0.toByte())) {
                                                        hidState = true
                                                        hidVer = "  SE Ver:${String.format("%02x", hiVerByte)}-${String.format("%02x", loVerByte)}"
                                                    }
                                                }

                                                IDX_BMD_CHIP -> {
                                                    val hiVerByte = usbRxPacket.executeBytes.data[offset++]
                                                    val miVerByte = usbRxPacket.executeBytes.data[offset++]
                                                    val loVerByte = usbRxPacket.executeBytes.data[offset++]
                                                    if((hiVerByte!=0.toByte()) || (miVerByte!=0.toByte()) || (loVerByte!=0.toByte())) {
                                                        bmd_legic_State = true
                                                        bmd_legic_Ver = "  Ver: ${String.format("%02x", hiVerByte)}-${String.format("%02x", miVerByte)}-${String.format("%02x", loVerByte)}"
                                                    }
                                                }

                                                IDX_LEGIC_CHIP -> {
                                                    val verByte1 = usbRxPacket.executeBytes.data[offset++]
                                                    val verByte2 = usbRxPacket.executeBytes.data[offset++]
                                                    val verByte3 = usbRxPacket.executeBytes.data[offset++]
//                                                    val verByte4 = usbRxPacket.executeBytes.data[offset++]        // No space in buffer, lock gives up this byte
                                                    if((verByte1!=0.toByte()) || (verByte2!=0.toByte()) || (verByte3!=0.toByte())) {
                                                        useLEGIC = true
                                                        bmd_legic_State = true
                                                        bmd_legic_Ver = "  Ver: ${String.format("%02x", verByte1)}-${String.format("%02x", verByte2)}-${String.format("%02x", verByte3)}"
                                                    }
                                                }

                                                IDX_TOUCH_PAD -> {
                                                    if(usbRxPacket.executeBytes.data[offset++] == COMPONENT_READY) {
                                                        touchPadState = true
                                                    }
                                                }

                                                else -> {
                                                    view.showMessage("  Unsupported data in report.", true)
                                                }
                                            }

                                        }

                                        view.reportPromaryChipState(useLEGIC, hidState, hidVer, bmd_legic_State, bmd_legic_Ver, touchPadState)
                                        if(useLEGIC) {
                                            source.dumpTestingLogLine(mContext, " HID Chip: ${hidState}\n LEGIC Chip: ${bmd_legic_State}\n TouchPad: ${touchPadState}\n")
                                        } else {
                                            source.dumpTestingLogLine(mContext, " HID Chip: ${hidState}\n BMD Chip: ${bmd_legic_State}\n TouchPad: ${touchPadState}\n")
                                        }
                                    }

                                    TEST_STEP_SENSORS -> {
                                        processReport(WLRFRDM_LOCK_M_FIRMWARE, TEST_STEP_SENSORS)
                                    }

                                    TEST_STEP_RFID -> {
                                        processSingleTestingResult(TEST_STEP_RFID,"  RFID: OK. \n", "  RFID:FAIL \n")
                                    }

                                    TEST_MOBILE_ACCESS -> {
                                        processSingleTestingResult(TEST_MOBILE_ACCESS,"  RFID: OK. \n", "  RFID:FAIL \n")
                                    }

                                    TEST_STEP_EEPROM -> {
                                        processSingleTestingResult(TEST_STEP_EEPROM,"  EEPROM: OK \n", "  EEPROM: FAIL \n")
                                    }

                                    TEST_STEP_COMPLETE -> {
                                        processSingleTestingResult(TEST_STEP_COMPLETE,"  Test Complete: OK \n", "  Test Complete: FAIL \n")
                                    }

                                    else -> {
                                        view.showMessage("  Unsupported testing step.", true)
                                    }
                                }
                            }

                            WM_LOCK_M_FIRMWARE -> {
                                val testStep = usbRxPacket.executeBytes.data[OFFSET_TEST_STEP]
                                when(testStep) {
                                    TEST_BOARD_INFO -> {
                                        processReport(deviceType, TEST_BOARD_INFO)
                                    }

                                    TEST_STEP_RED_LED -> {
//                                        view.addTestReport("\n")
                                    }

                                    TEST_STEP_GREEN_LED -> {
//                                        view.addTestReport("\n\n")
                                    }

                                    TEST_STEP_SENSORS -> {
                                        processReport(WM_LOCK_M_FIRMWARE, TEST_STEP_SENSORS)
                                    }

                                    TEST_STEP_COMPLETE -> {
                                        processSingleTestingResult(TEST_STEP_COMPLETE,"  Test Complete: OK \n", "  Test Complete: FAIL \n")
                                    }

                                    else -> {
                                        view.showMessage("  Unsupported testing step.", true)
                                    }
                                }
                            }

                            else -> {
                                view.showMessage("  Unsupported device type", true)
                            }
                        }
                    }

                    else -> {
                        view.showMessage("Received unsupported command.", true)
                    }
                }
            }

            HOST_EXEC_BOARD_ERROR -> {
                view.showMessage("${MAP_COMMAND_NAME[rxPacket.executeBytes.cmd]} failed: Board has error", true)
            }

            HOST_EXEC_BOARD_TIMEOUT -> {
                view.showMessage("${MAP_COMMAND_NAME[rxPacket.executeBytes.cmd]} failed: Timeout", true)
            }

            HOST_EXEC_RESP_COLLISION -> {
                view.showMessage("${MAP_COMMAND_NAME[rxPacket.executeBytes.cmd]} failed: Response collision", true)
            }

            HOST_EXEC_PARAM_ERR -> {
                view.showMessage("${MAP_COMMAND_NAME[rxPacket.executeBytes.cmd]} failed: Parameters has error", true)
            }

            HOST_EXEC_UID_COLLISION -> {
                view.showMessage("${MAP_COMMAND_NAME[rxPacket.executeBytes.cmd]} failed: UID collision", true)
            }

            HOST_EXEC_UPSUPPORTED_ERROR -> {
                view.showMessage("${MAP_COMMAND_NAME[rxPacket.executeBytes.cmd]} failed: Upsupported.", true)
            }
            else -> {
                view.showMessage("${MAP_COMMAND_NAME[rxPacket.executeBytes.cmd]} failed: Unknown error", true)
            }

        }
    }

    private fun processTestWithTimer(test_step: Byte, errMsg: String, tickNum: Boolean) {
        var offset = OFFSET_TEST_RESULT
        val reportCount = usbRxPacket.executeBytes.data[offset++].toInt()

        if(reportCount > 0) {
            val reportType = usbRxPacket.executeBytes.data[offset++]

            when(reportType) {
                IDX_TIMER_PERIOD -> {
                    view.reportTestTimerPeriod(test_step, usbRxPacket.executeBytes.data[offset])
                    source.dumpTestingLogLine(mContext, "${MAP_TIMER_PERIOD_FRONT[test_step]} ${usbRxPacket.executeBytes.data[offset]} seconds ${MAP_TIMER_PERIOD_BACK[test_step]}\n")
                }

                IDX_TIMER_TICK -> {
                    if(tickNum) {
                        view.reportTestTimerTick(test_step, usbRxPacket.executeBytes.data[offset])
                        source.dumpTestingLogLine(mContext, ".${usbRxPacket.executeBytes.data[offset]}")
                    }
                    else {
                        view.reportTestTimerTick(test_step, usbRxPacket.executeBytes.data[offset])
                        source.dumpTestingLogLine(mContext, "--")
                    }

                }

                IDX_TEST_RESULT -> {
                    when(test_step) {
                        TEST_STEP_EEPROM, TEST_STEP_SRAM -> {
                            view.reportTestTimerTick(test_step, usbRxPacket.executeBytes.data[offset])
                            if(usbRxPacket.executeBytes.data[offset] == 1.toByte())
                                source.dumpTestingLogLine(mContext, "T")
                            else
                                source.dumpTestingLogLine(mContext, "F")
                        }

                        else -> {

                        }
                    }
                }

                else -> {
                    view.showMessage("${errMsg}", true)
                    source.dumpTestingLogLine(mContext, "${errMsg}")
                }
            }
        } else {
            when(test_step) {
                TEST_STEP_EEPROM, TEST_STEP_SRAM -> {
                    view.reportTestLabel(test_step)
                    source.dumpTestingLogLine(mContext, MAP_TEST_LABEL[test_step]!!)
                }
            }
        }
    }

    private fun processSingleTestingResult(test_step: Byte, msgPass: String, msgFail: String) {
        var offset = OFFSET_TEST_RESULT
        var rptCompCount = usbRxPacket.executeBytes.data[offset++]
        if(rptCompCount > 0) {
            when (usbRxPacket.executeBytes.data[offset++]) {
                IDX_TEST_RESULT -> {
                    if(usbRxPacket.executeBytes.data[offset++] == RESULT_PASS) {
                        view.reportTestResult(test_step, RESULT_PASS)
                        source.dumpTestingLogLine(mContext, "\n  ${msgPass}")
                    } else {
                        view.reportTestResult(test_step, RESULT_FAIL)
                        source.dumpTestingLogLine(mContext, "\n  ${msgFail}")
                    }
                }

                else -> {
                    view.showMessage("  Unexpected test result.", true)
                    source.dumpTestingLogLine(mContext, "\n  Unexpected test result")
                }
            }
        } else {
            view.showMessage("No result in testing report packet.", true)
        }
    }

    private fun processReport(deviceType: Int, test_step: Byte) {
        when(test_step) {
            TEST_BOARD_INFO -> {
                val version = "v.${usbRxPacket.executeBytes.data[LOCK_VER_OFFSET+0]}.${usbRxPacket.executeBytes.data[LOCK_VER_OFFSET+1]}.${usbRxPacket.executeBytes.data[LOCK_VER_OFFSET+2]}"
                val deviceName = MAP_UPGRADE_DEVICE_NAME[deviceType]
                var lockUID = "${covertByteArrayToString(usbRxPacket.executeBytes.lockSNBytes)}".replaceFirst("^0+(?!$)".toRegex(), "")

                if(deviceType == CTRLB_FIRMWARE) {
                    if(usbRxPacket.executeBytes.data[OFFSET_TEST_RESULT] == 0x01.toByte()) {
                        if(usbRxPacket.executeBytes.data[OFFSET_TEST_RESULT+1] == IDX_MCU_TYPE) {
                            if(usbRxPacket.executeBytes.data[OFFSET_TEST_RESULT+2] == CTRLB_MCU128) {
                                view.reportDeviceInfo("128-${deviceName}", version, "")
                                source.dumpTestingLogLine(mContext, "PIC24FJ128GA606 - ${deviceName}, ${version} \n")
                            } else {
                                view.reportDeviceInfo("256-${deviceName}", version, "")
                                source.dumpTestingLogLine(mContext, "PIC24FJ256GA606 - ${deviceName}, ${version} \n")
                            }
                        } else {
                            view.showMessage("Unsupported report result type in device infomation report", true)
                            source.dumpTestingLogLine(mContext, "Unsupported report result type in device infomation report")
                        }
                    } else {
                        view.showMessage("Unsupported report result number in device infomation report", true)
                        source.dumpTestingLogLine(mContext, "Unsupported report result number in device infomation report")
                    }
                } else {
                    view.reportDeviceInfo(deviceName!!, version, "UID:${lockUID}")
                    source.dumpTestingLogLine(mContext, "${deviceName}, ${version}, UID: ${lockUID} \n")
                }
            }

            TEST_STEP_SENSORS -> {
                var motorSensorState= SENSOR_IS_MISS
                var latchSensorState= SENSOR_IS_MISS
                var doorSensorState= SENSOR_IS_MISS

                var offset = OFFSET_TEST_STEP

                offset++        // move to report component count
                var rptCompCount = usbRxPacket.executeBytes.data[offset++]
                while(rptCompCount-- > 0) {
                    when (usbRxPacket.executeBytes.data[offset++]) {
                        IDX_DOOR_SENSOR -> {
                            if (usbRxPacket.executeBytes.data[offset++] == COMPONENT_READY) {
                                doorSensorState = SENSOR_IS_OK
                            } else {
                                doorSensorState = SENSOR_IS_FAIL
                            }
                        }

                        IDX_LATCH_SENSOR -> {
                            if (usbRxPacket.executeBytes.data[offset++] == COMPONENT_READY) {
                                latchSensorState = SENSOR_IS_OK
                            } else {
                                latchSensorState = SENSOR_IS_FAIL
                            }
                        }

                        IDX_MOTOR_SENSOR -> {
                            if (usbRxPacket.executeBytes.data[offset++] == COMPONENT_READY) {
                                motorSensorState = SENSOR_IS_OK
                            } else {
                                motorSensorState = SENSOR_IS_FAIL
                            }
                        }

                        else-> {
                            view.showMessage("  Unsupported data in report.", true)
                        }
                    }
                }

                view.reportSensorState(doorSensorState, latchSensorState, motorSensorState)
                source.dumpTestingLogLine(mContext, "\n Door ${MAP_SENSOR_STATE[doorSensorState]}\n Motor ${MAP_SENSOR_STATE[motorSensorState]}\n Latch ${MAP_SENSOR_STATE[latchSensorState]}\n")
            }

            else -> {
                view.showMessage("Unsupported test step.", true)
                source.dumpTestingLogLine(mContext, "Unsupported test step.")
            }
        }
    }

    override fun unsubscribe() {
        compositeDisposable.clear()
    }

    companion object {
        private val LOG_TAG = Presenter::class.java.simpleName
    }
}