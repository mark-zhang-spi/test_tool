package com.digilock.pivot_test_util

import android.app.Application
import com.digilock.pivot_test_util.dagger.*

class Application : Application() {

    private lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        // Dagger
        appComponent = DaggerAppComponent.builder()
                        .appModule(AppModule(this))
                        .build()

    }

    fun getAppComponent() = appComponent
}