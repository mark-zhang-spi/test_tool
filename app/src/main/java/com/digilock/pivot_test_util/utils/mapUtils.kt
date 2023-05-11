package com.digilock.pivot_test_util.utils

import com.digilock.pivot_test_util.serialservice.CtrlbState
import com.digilock.pivot_test_util.utils.constants.WLRFRDM_LOCK_L_FIRMWARE
import com.digilock.pivot_test_util.utils.constants.WLRFRDM_LOCK_M_FIRMWARE
import com.digilock.pivot_test_util.utils.constants.WM_LOCK_L_FIRMWARE
import com.digilock.pivot_test_util.utils.constants.WM_LOCK_M_FIRMWARE


val MAP_LOCKER_TYPE = mapOf(
    WLRFRDM_LOCK_M_FIRMWARE to "WLRFRDM_M",
    WLRFRDM_LOCK_L_FIRMWARE to "WLRFRDM_L",
    WM_LOCK_M_FIRMWARE to "SMWM_M",
    WM_LOCK_L_FIRMWARE to "SMWM_L"
)

val MAP_CTRLB_STATE = mapOf(
    CtrlbState.IS_STARTED to "Started",
    CtrlbState.IS_INITIALIZED to "Initialized",
    CtrlbState.IS_MOUNTED to "Mounted"
)
