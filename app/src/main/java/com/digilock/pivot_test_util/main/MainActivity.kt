package com.digilock.pivot_test_util.main

import android.Manifest
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.paris.Paris
import com.digilock.pivot_test_util.Application
import com.digilock.pivot_test_util.R
import com.digilock.pivot_test_util.auto_start.BootUpReceiver.Companion.CLOSE_ALL_PREVIOUS_INSTANCES
import com.digilock.pivot_test_util.serialservice.UsbService
import com.digilock.pivot_test_util.serialservice.UsbService.Companion.ACTION_CTRLB_CONNECTED
import com.digilock.pivot_test_util.serialservice.UsbService.Companion.ACTION_CTRLB_DISCONNECTED
import com.digilock.pivot_test_util.serialservice.UsbService.Companion.MESSAGE_FROM_USB_PORT
import com.digilock.pivot_test_util.serialservice.UsbService.Companion.MESSAGE_FROM_USB_STATE
import com.digilock.pivot_test_util.utils.Helper
import com.digilock.pivot_test_util.utils.constants.*
import com.digilock.pivot_test_util.utils.covertByteArrayToString
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.codec.binary.StringUtils
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity(),
                      Contract.View,
                      View.OnClickListener {
    private lateinit var systemPref: SharedPreferences
    var mLogFileIndex: Int = 0

    private lateinit var dataSource: DataSource
    private lateinit var presenter: Contract.Presenter

    private lateinit var allButtons: Array<Button>


    private var gIsLargeScreen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Helper.isLargeTablet(this.getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            gIsLargeScreen = true
        }

        mContext = this

        setupViews(savedInstanceState)
        presenter.start()

        systemPref = getSharedPreferences("SavedLogFileIndex", MODE_PRIVATE)
        mLogFileIndex = systemPref.getInt("LogFileIndex", 0)

        mUsbHandler = USBHandler(this)

        Log.i(LOG_TAG, "Bootup, Mainactivity is created !!")
        val filter = IntentFilter()
        filter.addAction(CLOSE_ALL_PREVIOUS_INSTANCES)
        registerReceiver(mBootReceiver, filter)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            presenter.initTestingLog(mContext, true)
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    private fun setupViews(savedInstanceState: Bundle?) {
        (application as Application).getAppComponent().inject(this)

        if (!this::dataSource.isInitialized) dataSource = Repository()
        if (!this::presenter.isInitialized) presenter = Presenter(this, dataSource)

        allButtons = arrayOf(btnSelfTestLock, btnSelfTestCtrlb , btnSaveTestLog)

        for (btn in allButtons) btn.setOnClickListener(this)

//        inactiveButtons(allButtons)
        activeButtons(allButtons)


        if(gIsLargeScreen) {
            tvSpaceHolder.visibility = View.VISIBLE

            Paris.styleBuilder(btnSelfTestLock)
                .add(R.style.main_button)
                .apply()

            Paris.styleBuilder(btnSelfTestCtrlb)
                .add(R.style.main_button)
                .apply()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
        } else {
            presenter.initTestingLog(mContext, true)
        }

    }

    override fun onResume() {
        super.onResume()

        // USB module
        startUsbService(UsbService::class.java, usbConnection, null)     // Start UsbService(if it was not started before) and Bind it
    }

    override fun onClick(view: View) {
        when (view.id) {
            btnSelfTestLock.id -> {
                initTestLockReportLayout()
                presenter.testLock()
            }

            btnSelfTestCtrlb.id -> {
                initTestCtrlbReportLayout()

                presenter.testCtrlb()
            }

            btnSaveTestLog.id -> {
                mLogFileIndex = mLogFileIndex+1
                presenter.saveTestLog(mLogFileIndex)

                initTestCtrlbReportLayout()
                initTestLockReportLayout()
                
                // Save test log file index
                val myEdit: SharedPreferences.Editor = systemPref.edit()
                myEdit.putInt("LogFileIndex", mLogFileIndex)
                myEdit.apply()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        try {
//            unbindService(usbConnection)
            val usbService = Intent(this@MainActivity, UsbService::class.java)
            stopService(usbService)

            unregisterReceiver(mBootReceiver)
        } catch(e: Exception) {
            Log.e(LOG_TAG, "USB connection had no binded service !!")
        }
    }

    override fun onStart() {
        super.onStart()
    }


    override fun onDestroy() {
        super.onDestroy()

        presenter.unsubscribe()
    }


    /***** Contract Functions *****/
    private fun initTestLockReportLayout() {
        llDeviceInfo.visibility = View.INVISIBLE
        tvDeviceName.text = ""
        tvDeviceVersion.text = ""
        tvDeviceUID.text = ""

        llPrimaryChipState.visibility = View.GONE
        tvHidMark.text = ""
        tvHIDChip.text = ""
        tvHIDState.text = ""
        tvHIDVersion.text = ""

        tvBMD_LEGIC_Mark.text = ""
        tvBMD_LEGIC_Chip.text = ""
        tvBMD_LEGIC_State.text = ""
        tvBMD_LEGIC_Version.text = ""

        tvTouchPadMark.text = ""
        tvTouchPad.text = ""
        tvTouchPadState.text = ""

        llLED.visibility = View.INVISIBLE
        tvRedLed.text = ""
        tvRedLedTick.text = ""
        tvGreenLed.text = ""
        tvGreenLedTick.text = ""

        llTestSensors.visibility = View.INVISIBLE
        tvTestSensors.text = ""
        tvTestSensorsTick.text = ""

        tvDoorSensorMark.text = ""
        tvDoorSensor.text = ""
        tvDoorSensorState.text = ""

        tvMotorSensorMark.text = ""
        tvMotorSensor.text = ""
        tvMotorSensorState.text = ""

        tvLatchSensorMark.text = ""
        tvLatchSensor.text = ""
        tvLatchSensorState.text = ""

        llTestRFID.visibility = View.GONE
        tvTestRFID.text = ""
        tvTestRFIDTick.text = ""

        tvRFIDMark.text = ""
        tvRFID.text = ""
        tvRFIDState.text = ""

        llTestMobileID.visibility = View.GONE
        tvTestMobileID.text = ""
        tvTestMobileIDTick.text = ""

        tvMobileIDMark.text = ""
        tvMobileID.text = ""
        tvMobileIDState.text = ""

        llTestCtrlbCOM.visibility = View.GONE
        tvTestCtrlbCOM.text = ""
        tvTestCtrlbCOMTick.text = ""

        tvCtrlbCOMMark.text = ""
        tvCtrlbCOM.text = ""
        tvCtrlbCOMState.text = ""

        llTestEEPROM.visibility = View.GONE
        tvTestEEPROM.text = ""
        tvTestEEPROMTick.text = ""

        tvEEPROMMark.text = ""
        tvEEPROM.text = ""
        tvEEPROMState.text = ""

        llTestSRAM.visibility = View.GONE
        tvTestSRAM.text = ""
        tvTestSRAMTick.text = ""

        tvSRAMMark.text = ""
        tvSRAM.text = ""
        tvSRAMState.text = ""

        llTestDone.visibility = View.INVISIBLE
        tvTestDoneMark.text = ""
        tvTestDone.text = ""
        tvTestDoneState.text = ""
    }

    private fun initTestCtrlbReportLayout() {
        llDeviceInfo.visibility = View.INVISIBLE
        tvDeviceName.text = ""
        tvDeviceVersion.text = ""
        tvDeviceUID.text = ""

        llTestEEPROM.visibility = View.INVISIBLE
        tvTestEEPROM.text = ""
        tvTestEEPROMTick.text = ""

        tvEEPROMMark.text = ""
        tvEEPROM.text = ""
        tvEEPROMState.text = ""

        llTestSRAM.visibility = View.INVISIBLE
        tvTestSRAM.text = ""
        tvTestSRAMTick.text = ""

        tvSRAMMark.text = ""
        tvSRAM.text = ""
        tvSRAMState.text = ""

        llTestCtrlbCOM.visibility = View.INVISIBLE
        tvTestCtrlbCOM.text = ""
        tvTestCtrlbCOMTick.text = ""

        tvCtrlbCOMMark.text = ""
        tvCtrlbCOM.text = ""
        tvCtrlbCOMState.text = ""

        llTestDone.visibility = View.INVISIBLE
        tvTestDoneMark.text = ""
        tvTestDone.text = ""
        tvTestDoneState.text = ""

        llPrimaryChipState.visibility = View.GONE
        llLED.visibility = View.GONE
        llTestSensors.visibility = View.GONE
        llTestRFID.visibility = View.GONE
    }

    override fun reportDeviceInfo(name: String, ver:String, uid: String) {
        llDeviceInfo.visibility = View.VISIBLE

        tvDeviceName.text = name
        tvDeviceVersion.text = ver
        tvDeviceUID.text = uid
    }

    override fun reportPromaryChipState(useLegic: Boolean, hidState: Boolean, hidVer: String, bmd_legic_State: Boolean, bmd_legic_Ver: String, touchPadState: Boolean) {
        llPrimaryChipState.visibility = View.VISIBLE

        tvHIDChip.text = getString(R.string.hid_chip)
        if(hidState) {
            tvHidMark.text = markedTick
            tvHidMark.setTextColor(Color.GREEN)
            tvHIDState.text = "OK"
            tvHIDVersion.text = hidVer
        } else {
            tvHIDState.text = "FAIL"
            tvHidMark.text = markedCross
            tvHidMark.setTextColor(Color.RED)
        }

        var sTemp = ""
        if(useLegic) {
            sTemp = getString(R.string.legic_chip)
        } else {
            sTemp = getString(R.string.bmd_chip)
        }
        tvBMD_LEGIC_Chip.text = sTemp
        if (bmd_legic_State) {
            tvBMD_LEGIC_Mark.text = markedTick
            tvBMD_LEGIC_Mark.setTextColor(Color.GREEN)
            tvBMD_LEGIC_State.text = "OK"
            tvBMD_LEGIC_Version.text = bmd_legic_Ver
        } else {
            tvBMD_LEGIC_Mark.text = markedCross
            tvBMD_LEGIC_Mark.setTextColor(Color.RED)
            tvBMD_LEGIC_State.text = "FAIL"
        }

        tvTouchPad.text = getString(R.string.touch_pad)
        if(touchPadState) {
            tvTouchPadMark.text = markedTick
            tvTouchPadMark.setTextColor(Color.GREEN)
            tvTouchPadState.text = "OK"
        } else {
            tvTouchPadMark.text = markedCross
            tvTouchPadMark.setTextColor(Color.RED)
            tvTouchPadState.text = "FAIL"
        }
    }

    override fun reportSensorState(doorSensorState: Byte, latchSensorState: Byte, motorSensorState: Byte) {
        llSesnorState.visibility = View.VISIBLE

        if(doorSensorState == SENSOR_IS_OK) {
            tvDoorSensor.text = getString(R.string.door_sensor)
            tvDoorSensorMark.text = markedTick
            tvDoorSensorMark.setTextColor(Color.GREEN)

            tvDoorSensorState.text = "OK"
        } else if(doorSensorState == SENSOR_IS_FAIL){
            tvDoorSensor.text = getString(R.string.door_sensor)
            tvDoorSensorMark.text = markedCross
            tvDoorSensorMark.setTextColor(Color.RED)

            tvDoorSensorState.text = "FAIL"
//        } else {
//            tvDoorSensorMark.text = markedMiss
//            tvDoorSensorMark.setTextColor(Color.DKGRAY)

//            tvDoorSensorState.text = "MISS"
        }

        if(latchSensorState == SENSOR_IS_OK) {
            tvLatchSensor.text = getString(R.string.latch_sensor)
            tvLatchSensorMark.text = markedTick
            tvLatchSensorMark.setTextColor(Color.GREEN)

            tvLatchSensorState.text = "OK"
        } else if(latchSensorState == SENSOR_IS_FAIL){
            tvLatchSensor.text = getString(R.string.latch_sensor)
            tvLatchSensorMark.text = markedCross
            tvLatchSensorMark.setTextColor(Color.RED)

            tvLatchSensorState.text = "FAIL"
//        } else {
//            tvLatchSensorMark.text = markedMiss
//            tvLatchSensorMark.setTextColor(Color.DKGRAY)

//            tvLatchSensorState.text = "MISS"
        }

        if(motorSensorState == SENSOR_IS_OK) {
            tvMotorSensor.text = getString(R.string.motor_sensor)
            tvMotorSensorMark.text = markedTick
            tvMotorSensorMark.setTextColor(Color.GREEN)

            tvMotorSensorState.text = "OK"
        } else if(motorSensorState == SENSOR_IS_FAIL){
            tvMotorSensor.text = getString(R.string.motor_sensor)
            tvMotorSensorMark.text = markedCross
            tvMotorSensorMark.setTextColor(Color.RED)

            tvMotorSensorState.text = "FAIL"
//        } else {
//            tvMotorSensorMark.text = markedMiss
//            tvMotorSensorMark.setTextColor(Color.DKGRAY)

//            tvMotorSensorState.text = "MISS"
        }
    }


    override fun reportTestTimerPeriod(test_step: Byte, period: Byte) {
        when(test_step) {
            TEST_STEP_RED_LED -> {
                llLED.visibility = View.VISIBLE

                llRedLed.visibility = View.VISIBLE
                tvRedLed.text = "Red led will blink for ${period} seconds:"
            }

            TEST_STEP_GREEN_LED -> {
                llLED.visibility = View.VISIBLE

                llGreenLed.visibility = View.VISIBLE
                tvGreenLed.text = "Green led will blink for ${period} seconds:"
            }

            TEST_STEP_SENSORS -> {
                llTestSensors.visibility = View.VISIBLE

                tvTestSensors.text = "Close door in ${period} seconds to test sensors:"
                tvTestSensorsTick.text = "0"
            }

            TEST_STEP_RFID -> {
                llTestRFID.visibility = View.VISIBLE

                tvTestRFID.text = "Present card on lock in ${period} seconds to test RFID"
                tvTestRFIDTick.text = "0"
            }

            TEST_MOBILE_ACCESS -> {
                llTestMobileID.visibility = View.VISIBLE

                tvTestMobileID.text = "Present mobile device on lock in ${period} seconds for testing"
                tvTestMobileIDTick.text = "0"
            }

            TEST_CTRLB_COM -> {
                llTestCtrlbCOM.visibility = View.VISIBLE

                tvTestCtrlbCOM.text = "Connect a cabel between COM2 and COM5 in ${period} seconds:"
                tvTestCtrlbCOMTick.text = "0"
            }

            else -> {

            }
        }
    }

    override fun reportTestTimerTick(test_step: Byte, count: Byte) {
        when(test_step) {
            TEST_STEP_RED_LED -> {
                tvRedLedTick.text = "${tvRedLedTick.text}-- "
            }

            TEST_STEP_GREEN_LED -> {
                tvGreenLedTick.text = "${tvGreenLedTick.text}-- "
            }

            TEST_STEP_SENSORS -> {
                tvTestSensorsTick.text = "${tvTestSensorsTick.text}.${count}"
            }

            TEST_STEP_RFID -> {
                tvTestRFIDTick.text = "${tvTestRFIDTick.text}.${count}"
            }

            TEST_MOBILE_ACCESS -> {
                tvTestMobileIDTick.text = "${tvTestMobileIDTick.text}.${count}"
            }

            TEST_CTRLB_COM -> {
                tvTestCtrlbCOMTick.text = "${tvTestCtrlbCOMTick.text}.${count}"
            }

            TEST_STEP_EEPROM -> {
                if(count == 1.toByte()) {
                    tvTestEEPROMTick.text = "${tvTestEEPROMTick.text}T"
                } else {
                    tvTestEEPROMTick.text = "${tvTestEEPROMTick.text}F"
                }
            }

            TEST_STEP_SRAM -> {
                if(count == 1.toByte()) {
                    tvTestSRAMTick.text = "${tvTestSRAMTick.text}T"
                } else {
                    tvTestSRAMTick.text = "${tvTestSRAMTick.text}F"
                }
            }

            else -> {

            }
        }
    }

    override fun reportTestLabel(test_step: Byte) {
        when(test_step) {
            TEST_STEP_EEPROM -> {
                llTestEEPROM.visibility = View.VISIBLE
                tvTestEEPROM.text = "Test EEPROM:"
            }

            TEST_STEP_SRAM -> {
                llTestSRAM.visibility = View.VISIBLE
                tvTestSRAM.text = "Test SRAM:"
            }

            else -> {
            }
        }
    }

    override fun reportTestResult(test_step: Byte, result: Byte) {
        when(test_step) {
            TEST_STEP_RFID -> {
                tvRFID.text = getString(R.string.rfid)
                if(result == RESULT_PASS) {
                    tvRFIDMark.text = markedTick
                    tvRFIDMark.setTextColor(Color.GREEN)

                    tvRFIDState.text = "OK"
                } else {
                    tvRFIDMark.text = markedCross
                    tvRFIDMark.setTextColor(Color.RED)

                    tvRFIDState.text = "FAIL"
                }
            }

            TEST_MOBILE_ACCESS -> {
                tvMobileID.text = getString(R.string.mobile_access)
                if(result == RESULT_PASS) {
                    tvMobileIDMark.text = markedTick
                    tvMobileIDMark.setTextColor(Color.GREEN)

                    tvMobileIDState.text = "OK"
                } else {
                    tvMobileIDMark.text = markedCross
                    tvMobileIDMark.setTextColor(Color.RED)

                    tvMobileIDState.text = "FAIL"
                }
            }

            TEST_CTRLB_COM -> {
                tvCtrlbCOM.text = getString(R.string.com_port)
                if(result == RESULT_PASS) {
                    tvCtrlbCOMMark.text = markedTick
                    tvCtrlbCOMMark.setTextColor(Color.GREEN)

                    tvCtrlbCOMState.text = "OK"
                } else {
                    tvCtrlbCOMMark.text = markedCross
                    tvCtrlbCOMMark.setTextColor(Color.RED)

                    tvCtrlbCOMState.text = "FAIL"
                }
            }

            TEST_STEP_EEPROM -> {
                tvEEPROM.text = getString(R.string.eeprom)
                if(result == RESULT_PASS) {
                    tvEEPROMMark.text = markedTick
                    tvEEPROMMark.setTextColor(Color.GREEN)

                    tvEEPROMState.text = "OK"
                } else {
                    tvEEPROMMark.text = markedCross
                    tvEEPROMMark.setTextColor(Color.RED)

                    tvEEPROMState.text = "FAIL"
                }
            }

            TEST_STEP_SRAM -> {
                tvSRAM.text = getString(R.string.sram)
                if(result == RESULT_PASS) {
                    tvSRAMMark.text = markedTick
                    tvSRAMMark.setTextColor(Color.GREEN)

                    tvSRAMState.text = "OK"
                } else {
                    tvSRAMMark.text = markedCross
                    tvSRAMMark.setTextColor(Color.RED)

                    tvSRAMState.text = "FAIL"
                }
            }

            TEST_STEP_COMPLETE -> {
                llTestDone.visibility = View.VISIBLE
                tvTestDone.text = getString(R.string.test_done)
                if(result == RESULT_PASS) {
                    tvTestDoneMark.text = markedTick
                    tvTestDoneMark.setTextColor(Color.GREEN)

                    tvTestDoneState.text = "OK"
                } else {
                    tvTestDoneMark.text = markedCross
                    tvTestDoneMark.setTextColor(Color.RED)

                    tvTestDoneState.text = "FAIL"
                }
            }

            else -> {

            }
        }
    }

    override fun showMessage(msg: String, isError: Boolean) {
        val snackbar = Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG)
        snackbar.view.setBackgroundColor(R.color.blue)
        snackbar.show()
    }

    override fun setPresenter(p: Contract.Presenter) {
        presenter = p
        presenter.start()
    }


    /*
        USB module related
    */
    private fun startUsbService(service: Class<*>, serviceConnection: ServiceConnection, extras: Bundle?) {
        // Bind to a started service
        if (!UsbService.SERVICE_CONNECTED) {
            val serviceIntent = Intent(this, service)
            if (extras != null && !extras.isEmpty) {
                val keys = extras.keySet()
                for (key in keys) {
                    val extra = extras.getString(key)
                    serviceIntent.putExtra(key, extra)
                }
            }
            startService(serviceIntent)
        }

        val bindingIntent = Intent(this, service)
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /*
     * This handler will be passed to UartService. Data received from uart port is displayed through this handler
     */
    private class USBHandler(activity: MainActivity) : Handler(Looper.getMainLooper()) {
        private val mActivity: WeakReference<MainActivity>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_FROM_USB_PORT -> {
                    Log.d(LOG_TAG, "USB received a packet")

                    val data = msg.obj as String
                    mActivity.get()!!.presenter.processUSBPacket(data)
                }

                MESSAGE_FROM_USB_STATE -> {
                    Log.d(LOG_TAG, "USB state changed")

                    val data = msg.obj as String
                    when(data) {
                        ACTION_CTRLB_CONNECTED -> {
                            mActivity.get()?.connectControlBoard(true)
                        }

                        ACTION_CTRLB_DISCONNECTED -> {
                            mActivity.get()?.connectControlBoard(false)
                        }
                    }
                }

                else -> {

                }
            }
        }
    }

    private fun connectControlBoard(connected: Boolean) {
        if(connected) {
            tvConnState.text = "Connected"

            presenter.findCtrlb();
        } else {
            inactiveButtons(allButtons)
            tvConnState.text = "Disconnected"
        }
    }

    private var usbService: UsbService? = null
    private var mUsbHandler: USBHandler? = null

    private val usbConnection = object : ServiceConnection {
        override fun onServiceConnected(arg0: ComponentName, arg1: IBinder) {
            usbService = (arg1 as UsbService.UsbBinder).service
            usbService?.setHandler(mUsbHandler)

            usbService!!.connCtrlb()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            usbService = null
        }
    }

    override fun sendUsbCommand(packetDataBuf: ByteArray) {
        if(usbService != null) {
            usbService!!.write(packetDataBuf)
        }
    }
    /***** Private Functions *****/
    private fun activeButtons(activeButtons: Array<Button>) {
        for (btn in allButtons) btn.isEnabled = false
        for (btn in activeButtons) btn.isEnabled = true
    }

    private fun inactiveButtons(inactiveButtons: Array<Button>) {
        for (btn in allButtons) btn.isEnabled = true
        for (btn in inactiveButtons) btn.isEnabled = false
    }

    private val mBootReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                CLOSE_ALL_PREVIOUS_INSTANCES -> {
                    Log.i(LOG_TAG, "Received close command from Boot")

                    finishAndRemoveTask()
                }
            }
        }
    }

    companion object {
        private val LOG_TAG: String = MainActivity::class.java.simpleName

        const val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10

        private lateinit var mContext: Context
    }
}