package me.chamada.ft_hangouts.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.chamada.ft_hangouts.ContactApplication
import me.chamada.ft_hangouts.R
import me.chamada.ft_hangouts.adapters.ContactListAdapter
import me.chamada.ft_hangouts.databinding.FragmentContactListBinding
import me.chamada.ft_hangouts.views.RecyclerViewIndexedScroller

class ListFragment : Fragment(), MenuProvider, DeleteDialogFragment.OnConfirmListener {
    private var searchQuery: CharSequence? = null
    private var hasSelection: Boolean = false

    private val deleteDialogFragment = DeleteDialogFragment(this)

    private var _binding: FragmentContactListBinding? = null
    private var _actionBar: ActionBar? = null
    private var _fab: FloatingActionButton? = null
    private var _appBarLayout: AppBarLayout? = null

    // These properties are only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val actionBar get() = _actionBar!!
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

    private inner class SelectionObserver:
        SelectionTracker.SelectionObserver<Long>() {
        private var origToolbarTitle: CharSequence? = null
        private var hadSelection = false
        private var previousSelectedItemCount = 0

        private fun onSelectionCountChanged(selectedItemCount: Int) {
            if (selectedItemCount != 0) {
                if (previousSelectedItemCount == 0) {
                    origToolbarTitle = actionBar.title
                }
                actionBar.title = "$selectedItemCount / ${adapter.itemCount}"
            }
            else {
                actionBar.title = origToolbarTitle
            }
        }

        private fun onSelectionStateChanged(hasSelection: Boolean) {
            if (hasSelection) fab.hide() else fab.show()

            activity?.invalidateMenu()
        }

        override fun onSelectionChanged() {
            val selectedItemCount = adapter.tracker?.selection?.size()?: 0

            hasSelection = selectedItemCount != 0

            if (selectedItemCount != previousSelectedItemCount) {
                onSelectionCountChanged(selectedItemCount)
                previousSelectedItemCount = selectedItemCount
            }

            if (hasSelection != hadSelection) {
                onSelectionStateChanged(hasSelection)

                hadSelection = hasSelection
            }
        }
    }

    private class ScrollerChangeListener(
            private val appBarLayout: AppBarLayout,
            private val fab: FloatingActionButton
    ): RecyclerViewIndexedScroller.OnScrollChangeListener {
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
        val activity = requireActivity() as AppCompatActivity

        activity.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        searchQuery = savedInstanceState?.getCharSequence(KEY_SEARCH_QUERY)

        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        _fab = activity.findViewById(R.id.fab)
        _actionBar = activity.supportActionBar

        _appBarLayout = activity.findViewById(R.id.appbar_layout)

        searchQuery?.let {
            adapter.filter.filter(it)
        }

        binding.apply {
            val scrollerChangeListener = ScrollerChangeListener(appBarLayout, fab)

            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            adapter.tracker = SelectionTracker.Builder(
                    "selection",
                    recyclerView,
                    ContactListAdapter.ContactItemKeyProvider(adapter),
                    ContactListAdapter.ContactDetailsProvider(recyclerView),
                    StorageStrategy.createLongStorage()
                )
                .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build()

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

        adapter.tracker?.addObserver(SelectionObserver())

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
        if (!hasSelection) {
            actionBar.apply {
                setDisplayShowHomeEnabled(false)
                setDisplayHomeAsUpEnabled(false)
            }

            menuInflater.inflate(R.menu.menu_contact_list, menu)

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
        else {
            actionBar.apply {
                setDisplayShowHomeEnabled(true)
                setDisplayHomeAsUpEnabled(true)
            }

            menuInflater.inflate(R.menu.menu_contact_selection, menu)
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when(hasSelection) {
            false -> when (item.itemId) {
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
            true -> when (item.itemId) {
                R.id.action_selection_delete -> {
                    deleteDialogFragment
                        .show(requireActivity().supportFragmentManager, "DELETE_CONTACTS_DIALOG")

                    true
                }
                android.R.id.home -> {
                    adapter.tracker?.clearSelection()

                    true
                }
                else -> false
            }
        }
    }

    override fun onConfirmDelete() {
        adapter.tracker?.selection?.forEach { id -> viewModel.delete(id.toInt()) }
    }
}