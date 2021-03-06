/*
 * Create by Sungbin Ji on 2021. 1. 30.
 * Copyright (c) 2021. Sungbin Ji. All rights reserved. 
 *
 * SpakChat license is under the MIT license.
 * SEE LICENSE: https://github.com/sungbin5304/SpakChat/blob/master/LICENSE
 */

package me.sungbin.spakchat.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import me.sungbin.spakchat.chat.ChatViewModel
import me.sungbin.spakchat.chat.database.ChatDatabase
import me.sungbin.spakchat.user.UserUtil
import me.sungbin.spakchat.user.UserViewModel
import me.sungbin.spakchat.user.database.UserDatabase

abstract class BaseFragment : Fragment() {

    val userVm = UserViewModel.instance()
    val chatVm = ChatViewModel.instance()
    val storage = Firebase.storage.reference
    val firestore = Firebase.firestore
    val database = Firebase.database.reference.apply { keepSynced(true) }
    val userDb by lazy { UserDatabase.instance(requireContext()) }
    val chatDb by lazy { ChatDatabase.instance(requireContext()) }
    val userUtil by lazy { UserUtil.instance(firestore, userDb, requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
