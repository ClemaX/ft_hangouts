package me.chamada.ft_hangouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import me.chamada.ft_hangouts.databinding.FragmentContactListBinding


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ListFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by activityViewModels {
        ContactViewModelFactory((requireContext().applicationContext as ContactApplication).repository)
    }

    private fun editContact(contact: Contact? = null) {
        viewModel.current = contact
        findNavController().navigate(R.id.action_ListFragment_to_EditFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactListBinding.inflate(inflater, container, false)

        val recyclerView = binding.recyclerView
        val fastScroller = binding.fastScroller

        val adapter = ContactListSectionAdapter { contact, _ -> editContact(contact) }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        fastScroller.recyclerView = recyclerView

        viewModel.all.observe(viewLifecycleOwner) { contacts ->
            contacts?.let { adapter.submitList(contacts) }
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.fab.setOnClickListener { editContact() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}