package com.example.myapplication

import android.app.Application
import timber.log.Timber

class FluxApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
    }
}