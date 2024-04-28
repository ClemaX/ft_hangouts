package me.chamada.ft_hangouts

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

fun interface OnContactClickListener {
    fun onClick(contact: Contact, view: View?)
}

open class ContactListAdapter(private val clickListener: OnContactClickListener?) :
    ListAdapter<Contact, ContactListAdapter.ContactViewHolder>(ContactComparator()) {
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
                } else {
                    contact.phoneNumber.slice(IntRange(0, 1))
                }

                return initialsString
            }
        }

        fun bind(contact: Contact?) {
            if (contact != null) {
                val name = contact.name.ifEmpty { contact.phoneNumber }

                contactNameView.text = name
                contactPhoneNumberView.text = contact.phoneNumber
                contactInitialsTextView.text = getInitials(contact)

                val accentHue = abs(name.hashCode() + contact.id * 64) % 360

                val hsv = floatArrayOf(accentHue.toFloat(), 0.75f, 0.66f)

                contactInitialsBackground.drawable.mutate().setTint(Color.HSVToColor(hsv))

                if (clickListener != null)
                    itemView.setOnClickListener {
                        clickListener.onClick(contact, itemView)
                    }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder.create(parent, clickListener)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val current = getItem(position)

        return holder.bind(current)
    }
}