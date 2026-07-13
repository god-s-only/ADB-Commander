package com.adbcommand.app

import android.app.Application
import com.adbcommand.app.data.remote.ShizukuManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ADBCommanderApp: Application() {

    @Inject
    lateinit var shizukuManager: ShizukuManager

    override fun onCreate() {
        super.onCreate()
        shizukuManager.initialize()
    }

    override fun onTerminate() {
        super.onTerminate()
        shizukuManager.destroy()
    }
}