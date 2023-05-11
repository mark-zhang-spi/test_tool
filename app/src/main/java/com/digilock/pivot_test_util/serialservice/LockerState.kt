package com.digilock.pivot_test_util.serialservice


enum class LockerState {
    IS_UNLOCK, IS_LOCKED, UNKNOWN;

    companion object {
        fun fromByte(state: Byte): LockerState {
            return when(state) {
                EX_LOCKER_LOCKED -> IS_LOCKED
                EX_LOCKER_UNLOCKED -> IS_UNLOCK
                else -> UNKNOWN
            }
        }
    }
}