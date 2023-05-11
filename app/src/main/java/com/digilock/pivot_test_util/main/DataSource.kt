package com.digilock.pivot_test_util.main

import android.content.Context

interface DataSource {
    fun initTestingLog(context: Context, append: Boolean)
    fun dumpTestingLogLine(context: Context, uartLogLine: String)
    fun saveTestLog(logIndex: Int): Boolean
}