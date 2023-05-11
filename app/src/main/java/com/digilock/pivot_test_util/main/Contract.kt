package com.digilock.pivot_test_util.main

import android.content.Context
import com.digilock.pivot_test_util.base.BasePresenter
import com.digilock.pivot_test_util.base.BaseView

interface Contract {
    interface View : BaseView<Presenter> {
        fun showMessage(msg: String, isError: Boolean)
        fun sendUsbCommand(packetDataBuf: ByteArray)

        fun reportDeviceInfo(name: String, ver:String, uid: String)
        fun reportPromaryChipState(useLegic: Boolean, hidState: Boolean, hidVer: String, bmd_legic_State: Boolean, bmd_legic_Ver: String, touchPadState: Boolean)
        fun reportSensorState(doorSensorState: Byte, latchSensorState: Byte, motorSensorState: Byte)
        fun reportTestLabel(test_step: Byte)
        fun reportTestTimerPeriod(test_step: Byte, period: Byte)
        fun reportTestTimerTick(test_step: Byte, count: Byte)
        fun reportTestResult(test_step: Byte, result: Byte)
    }

    interface Presenter: BasePresenter {
        fun initTestingLog(context: Context, append: Boolean)

        fun connectDevice()

        fun findCtrlb()     // Send "Find Control Board" command

        fun initializeLock()
        fun testCtrlb()
        fun testLock()
        fun saveTestLog(logIndex: Int)

        fun processUSBPacket(data: String)
        fun unsubscribe()
    }
}