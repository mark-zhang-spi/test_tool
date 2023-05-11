package com.digilock.pivot_test_util.main

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import com.digilock.pivot_test_util.utils.constants.DOWNLOAD_DIR_NAME
import com.digilock.pivot_test_util.utils.constants.NEW_LINE
import com.digilock.pivot_test_util.utils.constants.TESTING_LOG_FILE_NAME
import com.digilock.pivot_test_util.utils.constants.formatter
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.time.LocalDateTime

class Repository(): DataSource {
    override fun initTestingLog(context: Context, append: Boolean) {
        val strDestDir = Environment.getExternalStorageDirectory().getPath()

        try {
            val dir = File(strDestDir + DOWNLOAD_DIR_NAME)
            if (!dir.exists()) dir.mkdirs()
            if (!dir.exists()) {
                Log.i(LOG_TAG, "Failed to create testing log folder.")
                return
            }

            val uartLogFile = File(dir, TESTING_LOG_FILE_NAME)
            try {
                if(!append && uartLogFile.exists()) {
                    uartLogFile.delete()
                }

                uartLogFile.setExecutable(true)
                uartLogFile.setReadable(true)
                uartLogFile.setWritable(true)

                val fs = FileOutputStream(uartLogFile, true)
                val pw = PrintWriter(fs)

                pw.println("******** TESTING Log File ********${NEW_LINE}")
                pw.println("         ${ LocalDateTime.now().format(formatter)} ${NEW_LINE}${NEW_LINE}")

                pw.flush()
                pw.close()
                fs.close()

                MediaScannerConnection.scanFile(context, arrayOf(uartLogFile.toString()), null, null)
            } catch (e: Exception) {
                Log.i(LOG_TAG, "Error happened to save TESTING log file: ${e.message}")
            }
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error happened to create download folder: ${e.message}")
        }
    }

    override fun dumpTestingLogLine(context: Context, uartLogLine: String) {
        val strDestDir = Environment.getExternalStorageDirectory().getPath()

        try {
            val dir = File(strDestDir + DOWNLOAD_DIR_NAME)
            if (!dir.exists()) dir.mkdirs()
            if (!dir.exists()) {
                Log.i(LOG_TAG, "Failed to create download folder.")
                return
            }

            val testLogFile = File(dir, TESTING_LOG_FILE_NAME)
            val logFileExist = testLogFile.exists()
            try{
                testLogFile.setExecutable(true)
                testLogFile.setReadable(true)
                testLogFile.setWritable(true)

                val fs = FileOutputStream(testLogFile, true)
                val pw = PrintWriter(fs)

                if(!logFileExist) {
                    pw.println("******** TESTING Log File ********${NEW_LINE}")
                    pw.println("         ${ LocalDateTime.now().format(formatter)} ${NEW_LINE}${NEW_LINE}")
                }

                pw.append(uartLogLine)

                pw.flush()
                pw.close()
                fs.close()

                MediaScannerConnection.scanFile(context, arrayOf(testLogFile.toString()), null, null)
            } catch (e: Exception) {
                Log.i(LOG_TAG, "Error happened to save testing log file: ${e.message}")
            }
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error happened to create download folder: ${e.message}")
        }
    }

    override fun saveTestLog(logIndex: Int): Boolean {
        // Copy test log file
        val strDestDir = Environment.getExternalStorageDirectory().getPath()

        try {
            val dir = File(strDestDir + DOWNLOAD_DIR_NAME)
            if (!dir.exists()) {
                Log.i(LOG_TAG, "Download folder does not exist.")
                return false
            }

            val testLogFile = File(dir, TESTING_LOG_FILE_NAME)
            if(!testLogFile.exists()) {
                Log.i(LOG_TAG, "Test log file does not exist.")
                return false
            }

            val v = TESTING_LOG_FILE_NAME.split('.')
            val savedLogFile = File(dir, v[0]+"_${logIndex}.${v[1]}")
            testLogFile.copyTo(savedLogFile, true)
        } catch(ex: Exception) {
            Log.e(LOG_TAG, ex.message)
            return false
        }

        return true
    }

    companion object {
        private val LOG_TAG = Repository::class.java.simpleName
    }
}