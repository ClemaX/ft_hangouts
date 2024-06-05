package me.chamada.ft_hangouts.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.chamada.ft_hangouts.R
import me.chamada.ft_hangouts.databinding.ListElementMessageInterlocutorBinding
import me.chamada.ft_hangouts.databinding.ListElementMessageOwnBinding
import java.lang.IllegalArgumentException

class SmsCursorAdapter(context: Context): RecyclerView.Adapter<SmsCursorAdapter.MessageViewHolder>() {
    private val contentResolver = context.contentResolver

    private var phoneNumber: String? = null
    private var cursor: Cursor? = null

    abstract class MessageViewHolder(protected val view: View):
        RecyclerView.ViewHolder(view) {

        abstract fun bind(cursor: Cursor)
    }

    abstract class TextMessageViewHolder(view: View) :
        MessageViewHolder(view) {

        data class TextMessage(
            val address: String,
            val body: String,
            val date: Long,
            val type: Int
        )
        override fun bind(cursor: Cursor) {
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)

            val address = cursor.getString(addressIndex)
            val body = cursor.getString(bodyIndex)
            val date = cursor.getLong(dateIndex)
            val type = cursor.getInt(typeIndex)

            val smsData = TextMessage(address, body, date, type)

            bindMessage(smsData)
        }

        abstract fun bindMessage(message: TextMessage)
    }

    class OwnMessageViewHolder(private val binding: ListElementMessageOwnBinding):
        TextMessageViewHolder(binding.root) {
        companion object {
            fun create(parent: ViewGroup): OwnMessageViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListElementMessageOwnBinding.inflate(inflater, parent, false)

                return OwnMessageViewHolder(binding)
            }
        }

        override fun bindMessage(message: TextMessage) {
            binding.apply {
                content.text = message.body
            }
        }
    }

    class InterlocutorMessageViewHolder(private val binding: ListElementMessageInterlocutorBinding):
        TextMessageViewHolder(binding.root) {
        companion object {
            fun create(parent: ViewGroup): InterlocutorMessageViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListElementMessageInterlocutorBinding.inflate(inflater, parent, false)

                return InterlocutorMessageViewHolder(binding)
            }
        }

        override fun bindMessage(message: TextMessage) {
            println(message.address)
            binding.apply {
                content.text = message.body
            }
        }
    }

    init {
        setHasStableIds(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun swapCursor(newCursor: Cursor?) {
        if (cursor == newCursor) return

        cursor?.close()
        cursor = newCursor

        notifyDataSetChanged()
    }

    fun setPhoneNumber(newPhoneNumber: String) {
        phoneNumber = newPhoneNumber
        notifySmsDataChanged()
    }

    fun notifySmsDataChanged() {
        if (phoneNumber.isNullOrEmpty()) {
            swapCursor(null)
            return
        }

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )
        val selection = "${Telephony.Sms.ADDRESS} = ?"
        val selectionArgs = arrayOf(phoneNumber)
        val sortOrder = "${Telephony.Sms.DATE} ASC"

        val newCursor = contentResolver?.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        swapCursor(newCursor)
    }

    override fun getItemCount(): Int {
        return cursor?.count ?: 0
    }

    override fun getItemId(position: Int): Long {
        var id: Long = -1L

        cursor?.apply {
            moveToPosition(position)
            id = getLong(getColumnIndexOrThrow(Telephony.Sms._ID))
        }

        return id
    }

    override fun getItemViewType(position: Int): Int {
        println("Getting item view type for position ${position}...")

        var viewType: Int = -1

        cursor?.apply {
            moveToPosition(position)

            val messageType = getInt(getColumnIndexOrThrow(Telephony.Sms.TYPE))

            viewType = when (messageType) {
                Telephony.Sms.MESSAGE_TYPE_SENT -> R.layout.list_element_message_own
                Telephony.Sms.MESSAGE_TYPE_INBOX -> R.layout.list_element_message_interlocutor
                else -> {
                    println("Warning: Unsupported message type: $messageType")
                    return R.layout.list_element_message_own
                }
            }
        }

        return viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return when (viewType) {
            R.layout.list_element_message_own -> OwnMessageViewHolder.create(parent)
            R.layout.list_element_message_interlocutor -> InterlocutorMessageViewHolder.create(parent)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        cursor?.apply {
            moveToPosition(position)
            holder.bind(this)
        }
    }
}