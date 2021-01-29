/*
 * Create by Sungbin Ji on 2021. 1. 29.
 * Copyright (c) 2021. Sungbin Ji. All rights reserved. 
 */

package me.sungbin.spakchat.ui.activity

import androidx.appcompat.app.AppCompatActivity
import me.sungbin.spakchat.R

abstract class BaseActivity : AppCompatActivity() {

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
