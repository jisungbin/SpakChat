/*
 * Create by Sungbin Ji on 2021. 2. 6.
 * Copyright (c) 2021. Sungbin Ji. All rights reserved.
 *
 * SpakChat license is under the MIT license.
 * SEE LICENSE: https://github.com/sungbin5304/SpakChat/blob/master/LICENSE
 */

package me.sungbin.spakchat.chat.activity

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import me.sungbin.androidutils.util.Logger
import me.sungbin.spakchat.R
import me.sungbin.spakchat.databinding.LayoutChatOtherBinding
import me.sungbin.spakchat.databinding.LayoutChatOwnBinding
import me.sungbin.spakchat.chat.model.Message
import me.sungbin.spakchat.chat.model.MessageViewType
import me.sungbin.spakchat.user.model.User

class ChatAdapter(private val me: User) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val differ = AsyncListDiffer(this, ChatDiffItemCallback())

    inner class OtherChatViewHolder(
        private val binding: LayoutChatOtherBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindViewHolder(message: Message) {
            with(binding) {
                this.message = message
                ivProfile.setOnClickListener {
                    val context = it.context
                    val transitionName = context.getString(R.string.image_transition)
                    val intent = Intent(context, DetailImageActivity::class.java)
                        .putExtra("image", message.owner?.profileImageColor)
                        .putExtra("name", message.owner?.name)
                        .putExtra("avatar", message.owner?.profileImageColor)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        context as Activity,
                        ivProfile, transitionName
                    )
                    context.startActivity(intent, options.toBundle())
                }
            }
        }
    }

    inner class OwnChatViewHolder(
        private val binding: LayoutChatOwnBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindViewHolder(message: Message) {
            with(binding) {
                this.message = message
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) = when (viewType) {
        MessageViewType.OWN -> OwnChatViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(viewGroup.context),
                R.layout.layout_chat_own, viewGroup, false
            )
        )
        MessageViewType.OTHER -> OtherChatViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(viewGroup.context),
                R.layout.layout_chat_other, viewGroup, false
            )
        ) // todo: Create FEED viewholder.
        else -> {
            Logger.w(viewType)
            throw Exception("$viewType is unknown viewType.")
        }
    }

    override fun onBindViewHolder(@NonNull viewholder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (message.messageViewType == MessageViewType.FEED) {
            // todo: Create FEED viewholder.
        } else {
            if (message.owner!!.key == me.key) {
                (viewholder as OwnChatViewHolder).bindViewHolder(message)
            } else {
                (viewholder as OtherChatViewHolder).bindViewHolder(message)
            }
        }
    }

    fun submit(messages: List<Message>) {
        differ.submitList(messages)
    }

    private fun getItem(position: Int) = differ.currentList[position]

    override fun getItemCount() = differ.currentList.size
    override fun getItemId(position: Int) = position.toLong()
    override fun getItemViewType(position: Int) = with(getItem(position)) {
        if (owner!!.key == me.key) {
            me.sungbin.spakchat.chat.MessageViewType.OWN
        } else {
            me.sungbin.spakchat.chat.MessageViewType.OTHER
        }
    }
}