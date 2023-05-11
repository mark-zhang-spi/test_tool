package com.digilock.pivot_test_util.serialservice

import android.util.Log
import com.google.gson.Gson

class HostCommPacket_BL {
    val packetSize = HOST_PACKET_SIZE_BL
    var commonHeader: HostCommHeader
    var packetBuffer: ByteArray
    var executeBytes: ExecuteBytes_BL

    init {
        packetBuffer = ByteArray(packetSize)
        commonHeader = HostCommHeader()
        executeBytes = ExecuteBytes_BL()

        commonHeader.data[0] = HEADER_DATA
        commonHeader.data[1] = HEADER_DATA
        commonHeader.data[2] = HEADER_DATA
        commonHeader.data[3] = HEADER_DATA
        commonHeader.data[4] = HEADER_DATA
        commonHeader.data[5] = 0x00
        commonHeader.data[6] = 0x00
        commonHeader.data[7] = 0x00
    }

    fun fromBufferToExecuteBytes() {
        var offset = 0
//        for(x in 0 until COMM_HEADER_SIZE)
//            commonHeader.data[x] = packetBuffer[offset++]

        offset = COMM_HEADER_SIZE
        executeBytes.header = packetBuffer[offset++]
        executeBytes.cmd = packetBuffer[offset++]
        executeBytes.dataSize = packetBuffer[offset++]

        for (x in 0 until EXEC_BYTES_DATA_SIZE_BL)
            executeBytes.data[x] = packetBuffer[offset++]

        executeBytes.tail = packetBuffer[offset++]
        executeBytes.crcValue = packetBuffer[offset]
    }

    fun fromExecuteBytesToBuffer() {
        var offset = 0

        for(x in 0 until COMM_HEADER_SIZE)
            packetBuffer[offset++] = commonHeader.data[x]

        packetBuffer[offset++] = executeBytes.header
        packetBuffer[offset++] = executeBytes.cmd
        packetBuffer[offset++] = executeBytes.dataSize

        for (x in 0 until EXEC_BYTES_DATA_SIZE_BL)
            packetBuffer[offset++] = executeBytes.data[x]

        packetBuffer[offset++] = executeBytes.tail
        packetBuffer[offset++] = executeBytes.crcValue
        packetBuffer[offset++] = 0                      // Padding
    }

    fun cleanPacketBuffer(): Boolean {
        for (i in COMM_HEADER_SIZE until packetSize)
            packetBuffer[i] = 0x00

        Log.d(LOG_TAG, "cleanPacketBuffer")

        fromBufferToExecuteBytes();
        return  true
    }

    companion object {
        private val LOG_TAG: String = HostCommPacket_BL::class.java.simpleName
        fun clone(source: HostCommPacket_BL): HostCommPacket_BL {
            val stringCommonPacket = Gson().toJson(source, HostCommPacket_BL::class.java)
            return Gson().fromJson<HostCommPacket_BL>(stringCommonPacket, HostCommPacket_BL::class.java)
        }
    }
}