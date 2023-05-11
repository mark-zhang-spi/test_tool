package com.digilock.pivot_test_util.serialservice

enum class CtrlbState {
    IS_STARTED, IS_INITIALIZED, IS_MOUNTED, UNKNOWN;

    companion object {
        fun fromByte(state: Byte): CtrlbState {
            return when(state) {
                EX_CTRLB_IS_STARTED -> IS_STARTED
                EX_CTRLB_IS_INITIALIZED -> IS_INITIALIZED
                EX_CTRLB_IS_MOUNTED -> IS_MOUNTED
                else -> UNKNOWN
            }
        }
    }
}