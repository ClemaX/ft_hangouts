package me.chamada.ft_hangouts.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.chamada.ft_hangouts.ContactApplication
import me.chamada.ft_hangouts.R
import me.chamada.ft_hangouts.adapters.ContactListAdapter
import me.chamada.ft_hangouts.views.RecyclerViewIndexedScroller
import me.chamada.ft_hangouts.databinding.FragmentContactListBinding


class ListFragment : Fragment(), MenuProvider {
    private var searchQuery: CharSequence? = null

    private var _binding: FragmentContactListBinding? = null
    private var _fab: FloatingActionButton? = null
    private var _appBarLayout: AppBarLayout? = null

    // These properties are only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val fab get() = _fab!!
    private val appBarLayout get() = _appBarLayout!!

    private val viewModel: ContactViewModel by activityViewModels {
        val repository = (requireContext().applicationContext as ContactApplication).repository

        ContactViewModel.Factory(repository)
    }

    private val adapter = ContactListAdapter { contact, _ -> viewContact(contact.id) }

    companion object {
        const val KEY_SEARCH_QUERY = "searchQuery"
    }

    private inner class OnQueryTextListener :
        SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            searchQuery = newText
            adapter.filter.filter(newText)

            return false
        }
    }

    private class ScrollerChangeListener(
            private val appBarLayout: AppBarLayout,
            private val fab: FloatingActionButton
    ): RecyclerViewIndexedScroller.OnScrollChangeListener() {
        private var appBarWasExpanded: Boolean = true

        override fun onScrollStart() {
            val layoutParams = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams

            if (layoutParams.behavior is AppBarLayout.Behavior) {
                val appBarBehavior = layoutParams.behavior as AppBarLayout.Behavior
                appBarWasExpanded = appBarBehavior.topAndBottomOffset == 0
            }

            if (appBarWasExpanded) {
                appBarLayout.setExpanded(false)
            }
            fab.hide()
        }

        override fun onScrollEnd() {
            if (appBarWasExpanded) {
                appBarLayout.setExpanded(true)
            }
            fab.show()
        }
    }

    private fun editContact(id: Int = 0) {
        val action = ListFragmentDirections.actionListFragmentToEditFragment()

        viewModel.select(id)
        findNavController().navigate(action)
    }

    private fun viewContact(id: Int) {
        val action = ListFragmentDirections.actionListFragmentToDetailsFragment()

        viewModel.select(id)
        findNavController().navigate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activity = requireActivity()

        activity.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        searchQuery = savedInstanceState?.getCharSequence(KEY_SEARCH_QUERY)

        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        _fab = activity.findViewById(R.id.fab)

        searchQuery?.let {
            adapter.filter.filter(it)
        }

        _appBarLayout = activity.findViewById(R.id.appbar_layout)

        binding.apply {
            val scrollerChangeListener = ScrollerChangeListener(appBarLayout, fab)

            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            scroller.recyclerView = recyclerView

            scroller.setOnScrollChangeListener(scrollerChangeListener)
        }

        viewModel.select(0)

        viewModel.all.observe(viewLifecycleOwner) { contacts ->
            contacts?.let {
                adapter.submitList(contacts)
                binding.recyclerView.scheduleLayoutAnimation()
            }
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putCharSequence(KEY_SEARCH_QUERY, searchQuery)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        fab.setImageResource(android.R.drawable.ic_input_add)
        fab.setOnClickListener { editContact() }
        fab.show()
    }

    override fun onStop() {
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
            searchQuery?.let {
                isIconified = false
                setQuery(it, false)
            }


            val queryListener = OnQueryTextListener()

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
                val action = ListFragmentDirections.actionListFragmentToSettingsFragment()

                findNavController().navigate(action)

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