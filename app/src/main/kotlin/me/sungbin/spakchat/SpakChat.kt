/*
 * Create by Sungbin Ji on 2021. 1. 30.
 * Copyright (c) 2021. Sungbin Ji. All rights reserved. 
 *
 * SpakChat license is under the MIT license.
 * SEE LICENSE: https://github.com/sungbin5304/SpakChat/blob/master/LICENSE
 */

package me.sungbin.spakchat

import android.app.Application
import android.content.Context
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.hilt.android.HiltAndroidApp
import me.sungbin.spakchat.util.ExceptionUtil

@HiltAndroidApp
class SpakChat : Application() {

    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()

        context = applicationContext

        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 60
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            ExceptionUtil.except(Exception(throwable), applicationContext)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        GlideApp.with(applicationContext).onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        GlideApp.with(applicationContext).onTrimMemory(level)
    }
}
