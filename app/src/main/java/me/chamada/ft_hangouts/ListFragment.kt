package me.chamada.ft_hangouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.chamada.ft_hangouts.databinding.FragmentContactListBinding


class ListFragment : Fragment(), MenuProvider {
    private var _binding: FragmentContactListBinding? = null
    private var _fab: FloatingActionButton? = null

    // These properties are only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val fab get() = _fab!!

    private val viewModel: ContactViewModel by activityViewModels {
        val repository = (requireContext().applicationContext as ContactApplication).repository

        ContactViewModel.Factory(repository)
    }

    private val adapter = ContactListAdapter { contact, _ -> editContact(contact) }


    private class OnQueryTextListener(private val adapter: ContactListAdapter) :
        SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            adapter.filter.filter(newText)

            return false
        }
    }

    private class ScrollerChangeListener(private val fab: FloatingActionButton):
        RecyclerViewIndexedScroller.OnScrollChangeListener() {
        override fun onScrollStart() {
            fab.hide()
        }

        override fun onScrollEnd() {
            fab.show()
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
        val activity = requireActivity()

        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        _fab = activity.findViewById(R.id.fab)

        activity.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.apply {
            val scrollerChangeListener = ScrollerChangeListener(fab)

            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            scroller.recyclerView = recyclerView

            scroller.setOnScrollChangeListener(scrollerChangeListener)
        }

        viewModel.all.observe(viewLifecycleOwner) { contacts ->
            contacts?.let {
                adapter.submitList(contacts)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab.setImageResource(android.R.drawable.ic_input_add)
        fab.setOnClickListener { editContact() }
        fab.show()
    }

    override fun onStop() {
        //fab.hide()
        fab.setOnClickListener(null)
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
        _fab = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchMenuItem = menu.findItem(R.id.action_search)

        (searchMenuItem.actionView as SearchView?)?.apply {
            val queryListener = OnQueryTextListener(adapter)

            queryHint = resources.getString(R.string.search_contact)
            setOnQueryTextListener(queryListener)
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController().navigate(R.id.action_ListFragment_to_SettingsFragment)

                true
            }
            R.id.action_pre_seed -> {
                viewModel.preSeed()

                true
            }
            R.id.action_clear -> {
                viewModel.deleteAll()

                true
            }
            else -> false
        }
    }
}