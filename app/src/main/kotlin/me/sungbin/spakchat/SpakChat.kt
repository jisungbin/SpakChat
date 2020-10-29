package me.sungbin.spakchat

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import me.sungbin.spakchat.model.user.User


/**
 * Created by SungBin on 2020-09-11.
 */

@HiltAndroidApp
class SpakChat : Application() {

    companion object {
        lateinit var context: Context
        lateinit var me: User
    }

    override fun onCreate() {
        super.onCreate()

        context = applicationContext

        /*Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            ExceptionUtil.except(Exception(throwable), applicationContext)
        }*/
    }
}