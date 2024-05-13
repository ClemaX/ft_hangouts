package me.chamada.ft_hangouts.ui.conversations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.chamada.ft_hangouts.HangoutsApplication
import me.chamada.ft_hangouts.R
import me.chamada.ft_hangouts.adapters.ConversationListAdapter
import me.chamada.ft_hangouts.databinding.FragmentConversationListBinding
import me.chamada.ft_hangouts.ui.DeleteDialogFragment
import me.chamada.ft_hangouts.ui.SearchableFragment

class ConversationListFragment:
    SearchableFragment(R.string.search_conversation),
    MenuProvider,
    DeleteDialogFragment.OnConfirmListener {
    private var hasSelection: Boolean = false

    private val deleteDialogFragment = DeleteDialogFragment(this)

    private var _binding: FragmentConversationListBinding? = null
    private var _actionBar: ActionBar? = null
    private var _fab: FloatingActionButton? = null

    // These properties are only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val actionBar get() = _actionBar!!
    private val fab get() = _fab!!

    private val viewModel: ConversationViewModel by activityViewModels {
        val repository = (requireContext().applicationContext as HangoutsApplication).conversationRepository

        ConversationViewModel.Factory(repository)
    }

    private val adapter = ConversationListAdapter { conversation, _ -> viewConversation(conversation.conversation.id) }

    private fun viewConversation(conversationId: Long = 0) {
        val action = if (conversationId == 0L)
            ConversationListFragmentDirections.actionConversationListFragmentToContactPickFragment()
        else
            ConversationListFragmentDirections.actionConversationListFragmentToConversationDetailsFragment()

        viewModel.select(conversationId)

        findNavController().navigate(action)
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

    init {
        filter = adapter.filter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity

        activity.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        _binding = FragmentConversationListBinding.inflate(inflater, container, false)

        _actionBar = activity.supportActionBar
        _fab = activity.findViewById(R.id.fab)

        binding.apply {
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            adapter.tracker = SelectionTracker.Builder(
                "selection",
                recyclerView,
                ConversationListAdapter.ConversationItemKeyProvider(adapter),
                ConversationListAdapter.ConversationDetailsProvider(recyclerView),
                StorageStrategy.createLongStorage()
            )
                .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build()
        }

        viewModel.select(0)

        viewModel.all.observe(viewLifecycleOwner) { conversations ->
            println("Got ${conversations?.count()} conversations")
            conversations?.let {
                adapter.submitList(conversations)
                binding.recyclerView.scheduleLayoutAnimation()
            }
        }

        adapter.tracker?.addObserver(SelectionObserver())

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        fab.setImageResource(android.R.drawable.ic_input_add)
        fab.setOnClickListener { viewConversation() }
        fab.show()
    }

    override fun onStop() {
        fab.setOnClickListener(null)

        super.onStop()
    }

    override fun onConfirmDelete() {
        TODO("Not yet implemented")
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        if (!hasSelection) {
            actionBar.apply {
                setDisplayShowHomeEnabled(false)
                setDisplayHomeAsUpEnabled(false)
            }

            menuInflater.inflate(R.menu.menu_conversation_list, menu)
            super.onCreateMenu(menu, menuInflater)
        }
        else {
            actionBar.apply {
                setDisplayShowHomeEnabled(true)
                setDisplayHomeAsUpEnabled(true)
            }

            menuInflater.inflate(R.menu.menu_conversation_selection, menu)
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when(hasSelection) {
            false -> when (item.itemId) {
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
}