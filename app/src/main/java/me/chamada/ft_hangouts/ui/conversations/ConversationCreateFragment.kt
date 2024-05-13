package me.chamada.ft_hangouts.ui.conversations

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import me.chamada.ft_hangouts.data.model.contact.Contact
import me.chamada.ft_hangouts.ui.contacts.ContactPickFragment

class ConversationCreateFragment : ContactPickFragment() {
    private val viewModel: ConversationViewModel by activityViewModels()

    override fun onPickContact(contact: Contact) {
        viewModel.selectOrCreate(contact.phoneNumber)

        val action = ConversationCreateFragmentDirections.actionConversationCreateFragmentToConversationDetailsFragment()

        findNavController().navigate(action)
    }
}