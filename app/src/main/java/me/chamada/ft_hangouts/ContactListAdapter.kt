package me.chamada.ft_hangouts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

fun interface OnContactClickListener {
    fun onClick(contact: Contact, view: View?)
}

open class ContactListAdapter(private val clickListener: OnContactClickListener?) :
    ListAdapter<Contact, ContactListAdapter.ContactViewHolder>(ContactComparator()) {
    class ContactViewHolder(itemView: View, private val clickListener: OnContactClickListener?) :
        RecyclerView.ViewHolder(itemView) {
        private val contactNameView: TextView = itemView.findViewById(R.id.name)
        private val contactPhoneNumberView: TextView = itemView.findViewById(R.id.phoneNumber)

        companion object {
            fun create(
                parent: ViewGroup,
                clickListener: OnContactClickListener?
            ): ContactViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_element_contact, parent, false)

                return ContactViewHolder(view, clickListener)
            }
        }

        fun bind(contact: Contact?) {
            if (contact != null) {
                contactNameView.text = contact.name
                contactPhoneNumberView.text = contact.phoneNumber

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