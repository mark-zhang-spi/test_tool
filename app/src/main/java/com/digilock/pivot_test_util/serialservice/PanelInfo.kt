package com.digilock.pivot_test_util.serialservice

class PanelInfo {
    var boardState: Byte = 0
    val lockerInfo = ArrayList<LockerInfo>()

    fun initialize() {
        boardState = 0
        lockerInfo.clear()
    }

    companion object {
        private val LOG_TAG: String = PanelInfo::class.java.simpleName
    }
}