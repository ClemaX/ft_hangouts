package me.chamada.ft_hangouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
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
        val scroller = binding.scroller

        val adapter = ContactListAdapter { contact, _ -> editContact(contact) }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        scroller.recyclerView = recyclerView

        val scrollerScrollListener = RecyclerViewIndexedScroller.OnScrollListener(scroller)

        recyclerView.addOnScrollListener(scrollerScrollListener)

        viewModel.all.observe(viewLifecycleOwner) { contacts ->
            contacts?.let {
                /*val indices = contacts.map { contact ->
                    val index =
                        if (contact.name.isNotEmpty()) contact.name [0]
                        else contact.phoneNumber[0]

                    index.uppercase().toString()
                }.toSet()
                println(indices)*/

                adapter.submitList(contacts)
            }
        }

        val queryListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)

                return false
            }
        }

        binding.searchBar.setOnQueryTextListener(queryListener)

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