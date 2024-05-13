package me.chamada.ft_hangouts.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.chamada.ft_hangouts.R
import me.chamada.ft_hangouts.data.model.conversation.Message
import me.chamada.ft_hangouts.databinding.ListElementMessageInterlocutorBinding
import me.chamada.ft_hangouts.databinding.ListElementMessageOwnBinding
import java.lang.IllegalArgumentException

class MessageListAdapter:
    ListAdapter<Message, MessageListAdapter.MessageViewHolder>(MessageComparator()) {

    abstract class MessageViewHolder(val view: View):
        RecyclerView.ViewHolder(view) {
        abstract fun bind(message: Message)
    }

    class OwnMessageViewHolder(private val binding: ListElementMessageOwnBinding):
        MessageViewHolder(binding.root) {
        companion object {
            fun create(parent: ViewGroup): OwnMessageViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListElementMessageOwnBinding.inflate(inflater, parent, false)

                return OwnMessageViewHolder(binding)
            }
        }

        override fun bind(message: Message) {
            binding.apply {
                content.text = message.content
            }
        }
    }

    class InterlocutorMessageViewHolder(private val binding: ListElementMessageInterlocutorBinding):
        MessageViewHolder(binding.root) {
        companion object {
            fun create(parent: ViewGroup): InterlocutorMessageViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListElementMessageInterlocutorBinding.inflate(inflater)

                return InterlocutorMessageViewHolder(binding)
            }
        }

        override fun bind(message: Message) {
            binding.apply {
                content.text = message.content
            }
        }
    }

    class MessageComparator : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        println("Getting item view type for position ${position}...")
        return when(currentList[position].senderId) {
            -1L -> R.layout.list_element_message_own
            else -> R.layout.list_element_message_interlocutor
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return when (viewType) {
            R.layout.list_element_message_own -> OwnMessageViewHolder.create(parent)
            R.layout.list_element_message_interlocutor -> InterlocutorMessageViewHolder.create(parent)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}