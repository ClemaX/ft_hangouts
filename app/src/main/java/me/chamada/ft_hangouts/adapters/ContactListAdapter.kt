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
import androidx.recyclerview.widget.RecyclerView.NO_ID
import me.chamada.ft_hangouts.R
import me.chamada.ft_hangouts.views.RecyclerViewIndexedScroller
import me.chamada.ft_hangouts.data.model.contact.Contact
import kotlin.math.abs


class ContactListAdapter(private val clickListener: OnContactClickListener?) :
    ListAdapter<Contact, ContactListAdapter.ContactViewHolder>(ContactComparator()),
    Filterable,
    RecyclerViewIndexedScroller.IndexLabelListener {
    private var contactList: List<Contact>? = null
    private var contactFilter: Filter = ContactFilter()
    private var searchQuery: CharSequence? = null

    var tracker: SelectionTracker<Long>? = null

    companion object {
        fun filterContacts(contacts: List<Contact>, query: CharSequence?): List<Contact> {
            return if (!query.isNullOrBlank()) {
                val trimmedConstraint = query.trim()
                contacts.filter { contact ->
                    contact.name.contains(trimmedConstraint, ignoreCase = true)
                        || contact.phoneNumber.contains(trimmedConstraint, ignoreCase = true)
                }
            }
            else {
                contacts
            }
        }
    }

    fun interface OnContactClickListener {
        fun onClick(contact: Contact, view: View?)
    }

    class ContactViewHolder(itemView: View, private val clickListener: OnContactClickListener?) :
        RecyclerView.ViewHolder(itemView) {
        private val contactNameView: TextView = itemView.findViewById(R.id.name)
        private val contactPhoneNumberView: TextView = itemView.findViewById(R.id.phoneNumber)
        private val contactInitialsTextView: TextView = itemView.findViewById(R.id.initials)
        private val contactInitialsBackground: ImageView = itemView.findViewById(R.id.initials_background)

        companion object {
            fun create(
                parent: ViewGroup,
                clickListener: OnContactClickListener?
            ): ContactViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_element_contact, parent, false)

                return ContactViewHolder(view, clickListener)
            }

            fun getInitials(contact: Contact): String {
                val names = contact.name
                    .trim()
                    .split(Regex("""\s+"""))
                    .takeIf{ it.size > 1 || it[0].isNotEmpty() }
                    ?: emptyList()

                val initialsString = if (names.isNotEmpty()) {
                    names.foldIndexed("") { i, acc, name ->
                        when (i) {
                            0, 1, names.count() - 1 -> acc + name[0].uppercaseChar()
                            else -> acc
                        }
                    }
                } else if (contact.phoneNumber.isNotBlank()) {
                    contact.phoneNumber.substring(0, 1)
                } else {
                    "?"
                }

                return initialsString
            }
        }

        private fun getContactColor(contact: Contact): Int {
            val name = contact.name.ifEmpty { contact.phoneNumber }

            val accentHue = abs(name.hashCode() + contact.id * 64) % 360
            val hsv = floatArrayOf(accentHue.toFloat(), 0.75f, 0.66f)

            return Color.HSVToColor(hsv)
        }

        fun bind(contact: Contact?, isActivated: Boolean? = null) {
            if (contact != null) {
                val contactColor = getContactColor(contact)
                val initials = getInitials(contact)

                contactInitialsBackground.drawable.mutate().setTint(contactColor)
                contactInitialsTextView.text = initials

                contactNameView.text = contact.name
                contactPhoneNumberView.text = contact.phoneNumber

                if (clickListener != null)
                {
                    itemView.setOnClickListener {
                        clickListener.onClick(contact, itemView)
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

    class ContactItemKeyProvider(private val adapter: ContactListAdapter):
        ItemKeyProvider<Long>(SCOPE_MAPPED) {
        override fun getKey(position: Int): Long {
            return adapter.getItemId(position)
        }

        override fun getPosition(key: Long): Int {
            return adapter.getItemPosition(key)
        }
    }

    class ContactDetailsProvider(private val recyclerView: RecyclerView): ItemDetailsLookup<Long>() {
        override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(e.x, e.y)

            return view?.let {
                val viewHolder = recyclerView.getChildViewHolder(it)
                return (viewHolder as ContactViewHolder).getItemDetails()
            }
        }
    }

    class ContactComparator : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }

    inner class ContactFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults? {
            searchQuery = constraint

            val totalList = contactList

            if (totalList.isNullOrEmpty())
                return null

            val filteredList = filterContacts(totalList, constraint)

            val results = FilterResults()

            results.values = filteredList.toMutableList()
            results.count = filteredList.size

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null) {
                @Suppress("UNCHECKED_CAST")
                super@ContactListAdapter.submitList(results.values as MutableList<Contact>)
            }
        }
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder.create(parent, clickListener)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val current = getItem(position)

        return holder.bind(current, tracker?.isSelected(getItemId(position)))
    }

    override fun submitList(list: List<Contact>?) {
        contactList = list

        super.submitList(list)

        searchQuery?.let {
            filter.filter(searchQuery)
        }
    }

    override fun getFilter(): Filter {
        return contactFilter
    }

    override fun getIndexLabel(position: Int): String {
        val contact = getItem(position)?: return ""
        val name = contact.name.ifBlank { contact.phoneNumber.ifBlank { "?" } }

        return name[0].uppercase()
    }

    override fun getItemId(position: Int): Long {
        val contact = getItem(position)?: return NO_ID

        return contact.id.toLong()
    }

    fun getItemPosition(id: Long): Int {
        return contactList?.indexOfFirst { it.id == id.toInt() }?: RecyclerView.NO_POSITION
    }
}