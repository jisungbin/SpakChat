/*
 * Create by Sungbin Ji on 2021. 1. 30.
 * Copyright (c) 2021. Sungbin Ji. All rights reserved.
 *
 * SpakChat license is under the MIT license.
 * SEE LICENSE: https://github.com/sungbin5304/SpakChat/blob/master/LICENSE
 */

package me.sungbin.spakchat.ui.activity.join

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.random.Random
import me.sungbin.androidutils.extensions.startActivity
import me.sungbin.androidutils.extensions.toast
import me.sungbin.spakchat.R
import me.sungbin.spakchat.SpakViewModel
import me.sungbin.spakchat.databinding.LayoutDialogRegisterBinding
import me.sungbin.spakchat.di.Firestore
import me.sungbin.spakchat.di.Storage
import me.sungbin.spakchat.model.user.AccountStatus
import me.sungbin.spakchat.model.user.User
import me.sungbin.spakchat.ui.activity.MainActivity
import me.sungbin.spakchat.util.ColorUtil
import me.sungbin.spakchat.util.EncryptUtil
import me.sungbin.spakchat.util.ExceptionUtil
import me.sungbin.spakchat.util.KeyManager
import me.sungbin.spakchat.util.PrefUtil

@AndroidEntryPoint
class RegisterBottomDialog private constructor(private val vm: SpakViewModel) :
    BottomSheetDialogFragment() {

    @Firestore
    @Inject
    lateinit var firestore: FirebaseFirestore

    @Storage
    @Inject
    lateinit var storage: StorageReference

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
                            .with(requireActivity())
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
            binding.tilName.error = null
            binding.tilId.error = null
            binding.tilPassword.error = null
            binding.tilPasswordConfirm.error = null
            val name = binding.tietName.text.toString()
            val id = binding.tietId.text.toString()
            val password = binding.tietPassword.text.toString()
            val passwordConfirm = binding.tietPasswordConfirm.text.toString()
            // todo: 이렇게 if문을 return써서 하는게 나을까?? 아니면 원래대로 if-else 지옥으로 가야하나???
            if (name.isBlank() || id.isBlank() || password.isBlank() || passwordConfirm.isBlank()) {
                toast(getString(R.string.register_input_all))
                return@setOnClickListener
            }
            if (id.length < 5) {
                binding.tilId.error = getString(R.string.register_input_five_length)
                return@setOnClickListener
            }
            if (name.length > 16) {
                binding.tilName.error = getString(R.string.register_nickname_too_long)
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
                val imagePath = binding.ivProfile.tag.toString()
                storage // 프로필 사진 등록
                    .child("profile/$name/profile.${imagePath.substringAfterLast(".")}")
                    .putFile(imagePath.toUri())
                    .addOnSuccessListener {
                        it.storage.downloadUrl.addOnSuccessListener { uri ->
                            upload(name, id, password, uri)
                        }
                    }
                    .addOnFailureListener { exception ->
                        ExceptionUtil.except(exception, requireContext())
                    }
            } else {
                upload(name, id, password)
            }
        }
    }

    private fun upload(
        name: String,
        id: String,
        password: String,
        profileImageUri: Uri? = null,
    ) {
        val user = User(
            key = "${id.first().toInt()}${
            Random.nextInt(
                10000,
                100000
            )
            }${name.last().toInt()}".toLong(),
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
            friends = listOf(),
            sex = null,
            emoji = listOf(),
            black = listOf(),
            accountStatus = AccountStatus.UNVARIED,
            isTestMode = false
        )

        firestore.collection("users")
            .document(name)
            .set(user)
            .addOnSuccessListener {
                PrefUtil.save(requireContext(), KeyManager.User.NAME, name)
                PrefUtil.save(requireContext(), KeyManager.User.ID, id)
                PrefUtil.save(requireContext(), KeyManager.User.PASSWORD, password)
                toast(getString(R.string.register_done))
                vm.me = user
                onDestroy()
                startActivity<MainActivity>()
            }
            .addOnFailureListener { exception ->
                ExceptionUtil.except(exception, requireContext())
            }
    }

    companion object {
        private lateinit var registerBottomDialog: RegisterBottomDialog
        fun instance(vm: SpakViewModel): RegisterBottomDialog {
            if (!::registerBottomDialog.isInitialized) {
                registerBottomDialog = RegisterBottomDialog(vm)
            }
            return registerBottomDialog
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismiss()
        _binding = null
    }
}
