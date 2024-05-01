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


class ListFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by activityViewModels {
        ContactViewModelFactory((requireContext().applicationContext as ContactApplication).repository)
    }

    inner class OnQueryTextListener(private val adapter: ContactListAdapter) :
        SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            adapter.filter.filter(newText)

            return false
        }
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

        val adapter = ContactListAdapter { contact, _ -> editContact(contact) }

        binding.apply {
            val queryListener = OnQueryTextListener(adapter)
            val scrollerScrollListener = RecyclerViewIndexedScroller.OnScrollListener(scroller)

            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            scroller.recyclerView = recyclerView

            recyclerView.addOnScrollListener(scrollerScrollListener)
            searchBar.setOnQueryTextListener(queryListener)
        }

        viewModel.all.observe(viewLifecycleOwner) { contacts ->
            contacts?.let {
                adapter.submitList(contacts)
            }
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