/*
 * Create by Sungbin Ji on 2021. 1. 30.
 * Copyright (c) 2021. Sungbin Ji. All rights reserved.
 *
 * SpakChat license is under the MIT license.
 * SEE LICENSE: https://github.com/sungbin5304/SpakChat/blob/master/LICENSE
 */

package me.sungbin.spakchat.user.activity.join

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import kotlin.random.Random
import me.sungbin.androidutils.extensions.startActivity
import me.sungbin.androidutils.extensions.toast
import me.sungbin.spakchat.R
import me.sungbin.spakchat.databinding.LayoutDialogRegisterBinding
import me.sungbin.spakchat.ui.activity.MainActivity
import me.sungbin.spakchat.ui.fragment.BaseBottomSheetDialogFragment
import me.sungbin.spakchat.user.model.AccountStatus
import me.sungbin.spakchat.user.model.User
import me.sungbin.spakchat.util.ColorUtil
import me.sungbin.spakchat.util.EncryptUtil
import me.sungbin.spakchat.util.ExceptionUtil
import me.sungbin.spakchat.util.KeyManager
import me.sungbin.spakchat.util.PrefUtil
import me.sungbin.spakchat.util.Util

class RegisterBottomSheetDialog private constructor() : BaseBottomSheetDialogFragment() {

    private var _binding: LayoutDialogRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = LayoutDialogRegisterBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retainInstance = false

        binding.ivProfile.setOnClickListener {
            TedPermission.with(requireContext())
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {
                        FishBun
                            .with(requireParentFragment())
                            .setImageAdapter(GlideAdapter())
                            .setMaxCount(1)
                            .setMinCount(1)
                            .startAlbum()
                    }

                    override fun onPermissionDenied(deniedPermissions: List<String>) {
                        toast(getString(R.string.register_cant_load_picture))
                    }
                })
                .setRationaleMessage(getString(R.string.register_need_permission_for_load_picture))
                .setDeniedMessage(R.string.register_permission_denied)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .check()
        }

        binding.btnSignupDone.setOnClickListener {
            binding.tilId.error = null
            binding.tilPassword.error = null
            binding.tilPasswordConfirm.error = null
            val name = binding.tietName.text.toString()
            val id = binding.tietId.text.toString()
            val password = binding.tietPassword.text.toString()
            val passwordConfirm = binding.tietPasswordConfirm.text.toString()
            val key = Util.generateRandomKey(id, password)
            // todo: ????????? if?????? return?????? ????????? ??????????? ????????? ???????????? if-else ???????????? ???????????????
            if (name.isBlank() || id.isBlank() || password.isBlank() || passwordConfirm.isBlank()) {
                toast(getString(R.string.register_input_all))
                return@setOnClickListener
            }
            if (id.length < 5) {
                binding.tilId.error = getString(R.string.register_input_five_length)
                return@setOnClickListener
            }
            if (password.length < 8) {
                binding.tilPassword.error = getString(R.string.register_input_eight_length)
                return@setOnClickListener
            }
            if (password != passwordConfirm) {
                binding.tilPasswordConfirm.error =
                    getString(R.string.register_not_match_password_confirm)
                return@setOnClickListener
            }
            if (binding.ivProfile.tag != null) {
                val imageUri = binding.ivProfile.tag.toString()
                storage // ????????? ?????? ??????
                    .child("profile/$key/profile.${imageUri.substringAfterLast(".")}")
                    .putFile(imageUri.toUri())
                    .addOnSuccessListener {
                        it.storage.downloadUrl.addOnSuccessListener { uri ->
                            joinUser(key, name, id, password, uri)
                        }
                    }
                    .addOnFailureListener { exception ->
                        ExceptionUtil.except(exception, requireContext())
                    }
            } else {
                joinUser(key, name, id, password)
            }
        }
    }

    private fun joinUser(
        key: Long,
        name: String,
        id: String,
        password: String,
        profileImageUri: Uri? = null,
    ) {
        val user = User(
            key = key,
            userId = "$name${Random.nextInt(10000)}",
            loginId = id,
            password = EncryptUtil.encrypt(
                EncryptUtil.EncryptType.MD5,
                password
            ),
            name = name,
            profileImage = profileImageUri.toString(),
            profileImageColor = ColorUtil.randomColor,
            backgroundImage = null,
            statusMessage = getString(R.string.login_default_status_message),
            birthday = null,
            lastOnline = null,
            isOnline = true,
            rooms = mutableListOf(),
            friends = mutableListOf(),
            sex = null,
            emoji = mutableListOf(),
            black = mutableListOf(),
            accountStatus = AccountStatus.UNVARIED
        )

        firestore.collection("users")
            .document(key.toString())
            .set(user)
            .addOnSuccessListener {
                PrefUtil.save(requireContext(), KeyManager.User.KEY, key.toString())
                PrefUtil.save(requireContext(), KeyManager.User.ID, id)
                PrefUtil.save(requireContext(), KeyManager.User.PASSWORD, password)
                toast(getString(R.string.register_done))
                userVm.me = user
                onDestroy()
                startActivity<MainActivity>()
            }
            .addOnFailureListener { exception ->
                ExceptionUtil.except(exception, requireContext())
            }
    }

    companion object {
        private val instance = RegisterBottomSheetDialog()
        fun instance() = instance
    }

    override fun onDestroy() {
        super.onDestroy()
        dismiss()
        _binding = null
    }
}
