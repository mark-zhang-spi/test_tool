package com.digilock.pivot_test_util.scheduler

import io.reactivex.rxjava3.core.Scheduler

interface BaseScheduleProvider {
    fun computation(): Scheduler

    fun io(): Scheduler

    fun ui(): Scheduler
}