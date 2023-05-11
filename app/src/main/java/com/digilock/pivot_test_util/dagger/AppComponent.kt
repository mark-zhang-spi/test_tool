package com.digilock.pivot_test_util.dagger

import com.digilock.pivot_test_util.main.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(dependencies = [], modules = [(AppModule::class)])
interface AppComponent {
    fun inject(mainActivity: MainActivity)
}