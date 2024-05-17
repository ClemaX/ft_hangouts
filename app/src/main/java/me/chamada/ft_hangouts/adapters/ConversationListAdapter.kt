package me.chamada.ft_hangouts.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.chamada.ft_hangouts.R
import me.chamada.ft_hangouts.data.model.contact.Contact
import me.chamada.ft_hangouts.data.model.conversation.ConversationPreview
import kotlin.math.abs

class ConversationListAdapter(private val clickListener: OnConversationClickListener?) :
    ListAdapter<ConversationPreview,
            ConversationListAdapter.ConversationViewHolder>(ConversationComparator()),
    Filterable {
    private val conversationFilter = ConversationFilter()

    private var conversationList: List<ConversationPreview>? = null
    private var searchQuery: CharSequence? = null

    var tracker: SelectionTracker<Long>? = null

    companion object {
        fun filterConversations(conversations: List<ConversationPreview>, query: CharSequence?): List<ConversationPreview> {
            return if (!query.isNullOrBlank()) {
                val trimmedConstraint = query.trim()
                conversations.filter { conversation ->
                    //TODO: conversation.interlocutor.name.contains(trimmedConstraint, ignoreCase = true)
                    conversation.interlocutor.phoneNumber.contains(trimmedConstraint, ignoreCase = true)
                }
            }
            else {
                conversations
            }
        }
    }

    fun interface OnConversationClickListener {
        fun onClick(conversation: ConversationPreview, view: View?)
    }

    class ConversationViewHolder(itemView: View, private val clickListener: OnConversationClickListener?):
        RecyclerView.ViewHolder(itemView) {
        private val conversationNameView: TextView = itemView.findViewById(R.id.name)
        private val lastMessageView: TextView = itemView.findViewById(R.id.lastMessage)
        private val contactInitialsTextView: TextView = itemView.findViewById(R.id.initials)
        private val contactInitialsBackground: ImageView = itemView.findViewById(R.id.initialsBackground)
        private val unreadIndicatorView: ImageView = itemView.findViewById(R.id.unreadIndicator)

        companion object {
            fun create(
                parent: ViewGroup,
                clickListener: OnConversationClickListener?
            ): ConversationViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_element_conversation, parent, false)

                return ConversationViewHolder(view, clickListener)
            }
        }

        private fun getContactColor(contact: Contact): Int {
            val name = contact.name.ifEmpty { contact.phoneNumber }

            val accentHue = abs(name.hashCode() + contact.id * 64) % 360
            val hsv = floatArrayOf(accentHue.toFloat(), 0.75f, 0.66f)

            return Color.HSVToColor(hsv)
        }

        fun bind(conversation: ConversationPreview?, isActivated: Boolean? = null) {
            if (conversation != null) {
                val contactColor = getContactColor(Contact())// TODO: getContactColor(conversation.interlocutor)
                val initials = "?"// TODO: getInitials(conversation.interlocutor)

                contactInitialsBackground.drawable.mutate().setTint(contactColor)
                contactInitialsTextView.text = initials

                conversationNameView.text = conversation.contactName?:
                    conversation.interlocutor.phoneNumber
                if (conversation.lastMessageContent != null) {
                    //val senderName = conversation.lastMessageSenderName?: conversation.lastMessageSenderPhoneNumber
                    /*lastMessageView.text = context.getString(
                        R.string.message_with_sender,
                        senderName,
                        conversation.lastMessageContent
                    )*/
                    lastMessageView.text = "TODO"
                }

                if (clickListener != null)
                {
                    itemView.setOnClickListener {
                        clickListener.onClick(conversation, itemView)
                    }
                }

                itemView.isActivated = isActivated ?: false
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long = itemId
            }
    }

    class ConversationComparator: DiffUtil.ItemCallback<ConversationPreview>() {
        override fun areItemsTheSame(oldItem: ConversationPreview, newItem: ConversationPreview): Boolean {
            return oldItem.conversation.id == newItem.conversation.id
        }

        override fun areContentsTheSame(oldItem: ConversationPreview, newItem: ConversationPreview): Boolean {
            return oldItem == newItem
        }
    }

    inner class ConversationFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults? {
            searchQuery = constraint

            val totalList = conversationList

            if (totalList.isNullOrEmpty())
                return null

            val filteredList = filterConversations(totalList, constraint)

            val results = FilterResults()

            results.values = filteredList.toMutableList()
            results.count = filteredList.size

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null) {
                @Suppress("UNCHECKED_CAST")
                super@ConversationListAdapter.submitList(results.values as MutableList<ConversationPreview>)
            }
        }
    }

    class ConversationItemKeyProvider(private val adapter: ConversationListAdapter):
        ItemKeyProvider<Long>(SCOPE_MAPPED) {
        override fun getKey(position: Int): Long {
            return adapter.getItemId(position)
        }

        override fun getPosition(key: Long): Int {
            return adapter.getItemPosition(key)
        }
    }

    class ConversationDetailsProvider(private val recyclerView: RecyclerView): ItemDetailsLookup<Long>() {
        override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(e.x, e.y)

            return view?.let {
                val viewHolder = recyclerView.getChildViewHolder(it)
                return (viewHolder as ConversationViewHolder).getItemDetails()
            }
        }
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        return ConversationViewHolder.create(parent, clickListener)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val current = getItem(position)

        return holder.bind(current, tracker?.isSelected(getItemId(position)))
    }

    override fun submitList(list: List<ConversationPreview>?) {
        conversationList = list

        super.submitList(list)

        searchQuery?.let {
            filter.filter(searchQuery)
        }
    }

    override fun getFilter(): Filter {
        return conversationFilter
    }
    override fun getItemId(position: Int): Long {
        val conversation = getItem(position)?: return RecyclerView.NO_ID

        return conversation.conversation.id
    }

    fun getItemPosition(id: Long): Int {
        return conversationList?.indexOfFirst { it.conversation.id == id }?: RecyclerView.NO_POSITION
    }
}

