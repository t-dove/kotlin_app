package com.example.anonymous_chat
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.anonymous_chat.databinding.MessageItemIncomingBinding
import com.example.anonymous_chat.databinding.MessageItemOutgoingBinding
import kotlin.reflect.KFunction

interface MessageDeletedListener {
    fun onMessageDeleted(messageId: Int)
}

class MessagesAdapter(private val messages: MutableList<Message>, private val messageDeletedListener: MessageDeletedListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_INCOMING = 1
        private const val VIEW_TYPE_OUTGOING = 2
    }

    private fun removeMessage(position: Int, callback: Boolean, origID: Int) {
        messages.removeAt(position)
        notifyItemRemoved(position)
        if(callback){
            messageDeletedListener.onMessageDeleted(origID)
        }
    }
    fun deleteMessageById(messageId: Int,callback:Boolean) {
        val position = findMessagePositionById(messageId)
        if (position != -1) {
            removeMessage(position, callback, messageId)
        }

    }

    private fun findMessagePositionById(messageId: Int): Int {
        for ((index, message) in messages.withIndex()) {
            if (message.id == messageId) {
                return index
            }
        }
        return -1
    }
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isIncoming) VIEW_TYPE_INCOMING else VIEW_TYPE_OUTGOING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_INCOMING) {
            val binding: MessageItemIncomingBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.message_item_incoming, parent, false)
            IncomingMessageViewHolder(binding, this::deleteMessageById)
        } else {
            val binding: MessageItemOutgoingBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.message_item_outgoing, parent, false)
            OutgoingMessageViewHolder(binding, this::deleteMessageById)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is IncomingMessageViewHolder) {
            holder.bind(messages[position])
        } else if (holder is OutgoingMessageViewHolder) {
            holder.bind(messages[position])
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class IncomingMessageViewHolder(private val binding: MessageItemIncomingBinding, private val deleteMessageById:(Int, Boolean)->Unit) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.message = message
            binding.executePendingBindings()
            itemView.setOnLongClickListener {
                showContextMenu(itemView, message)
                true
            }
        }
        private fun showContextMenu(view: View, message: Message) {
            val popup = PopupMenu(view.context, view)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.menu_message_options, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_delete -> {
                        deleteMessageById(message.id, true)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    class OutgoingMessageViewHolder(private val binding: MessageItemOutgoingBinding, private val deleteMessageById:(Int, Boolean)->Unit) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.message = message
            binding.executePendingBindings()
            itemView.setOnLongClickListener {
                showContextMenu(itemView, message)
                true
            }
        }
        private fun showContextMenu(view: View, message: Message) {
            val popup = PopupMenu(view.context, view)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.menu_message_options, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_delete -> {
                        deleteMessageById(message.id, true)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
}
