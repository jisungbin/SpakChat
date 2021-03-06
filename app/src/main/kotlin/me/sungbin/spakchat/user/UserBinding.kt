/*
 * Create by Sungbin Ji on 2021. 2. 6.
 * Copyright (c) 2021. Sungbin Ji. All rights reserved. 
 *
 * SpakChat license is under the MIT license.
 * SEE LICENSE: https://github.com/sungbin5304/SpakChat/blob/master/LICENSE
 */

package me.sungbin.spakchat.user

import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import me.sungbin.spakchat.GlideApp
import me.sungbin.spakchat.R
import me.sungbin.spakchat.user.model.User

object UserBinding {

    infix fun String?.or(other: ColorDrawable): Any =
        if (this.toString() == "null") other else this!!

    @JvmStatic
    @BindingAdapter("spak_loadProfile")
    fun loadProfile(imageView: ImageView, user: User?) {
        GlideApp
            .with(imageView.context)
            .load(user!!.profileImage or ColorDrawable(user.profileImageColor!!))
            .into(imageView)
    }

    @JvmStatic
    @BindingAdapter("spak_loadStatus")
    fun loadStatus(imageView: ImageView, isOnline: Boolean) {
        imageView.setImageResource(
            if (isOnline) R.drawable.bg_shape_online
            else R.drawable.bg_shape_offline
        )
    }

    @JvmStatic
    @BindingAdapter("spak_loadLastOnline")
    fun loadLastOnline(textView: TextView, time: Long) {
        textView.text = when {
            time - Date().time in 0..(1000 * 60 * 30) -> "방금 전 까지 접속"
            else -> {
                val formatter = SimpleDateFormat("MM.dd kk:mm 까지 접속", Locale.KOREA)
                val date = Date()
                date.time = time
                formatter.format(date)
            }
        }
    }
}
