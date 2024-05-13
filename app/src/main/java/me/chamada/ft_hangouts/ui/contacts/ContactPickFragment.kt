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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.chamada.ft_hangouts.HangoutsApplication
import me.chamada.ft_hangouts.R
import me.chamada.ft_hangouts.adapters.ContactListAdapter
import me.chamada.ft_hangouts.data.model.contact.Contact
import me.chamada.ft_hangouts.databinding.FragmentContactListBinding
import me.chamada.ft_hangouts.ui.SearchableFragment
import me.chamada.ft_hangouts.views.RecyclerViewIndexedScroller

abstract class ContactPickFragment:
    SearchableFragment(R.string.search_contact), MenuProvider {
    private var _binding: FragmentContactListBinding? = null
    private var _actionBar: ActionBar? = null
    private var _appBarLayout: AppBarLayout? = null
    private var _fab: FloatingActionButton? = null

    // These properties are only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val appBarLayout get() = _appBarLayout!!
    private val fab get() = _fab!!

    private val viewModel: ContactViewModel by activityViewModels {
        val repository = (requireContext().applicationContext as HangoutsApplication).contactRepository

        ContactViewModel.Factory(repository)
    }

    private val adapter = ContactListAdapter { contact, _ -> onPickContact(contact) }

    abstract fun onPickContact(contact: Contact)

    private inner class ScrollerChangeListener:
        RecyclerViewIndexedScroller.OnScrollChangeListener {
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


        _binding = FragmentContactListBinding.inflate(inflater, container, false)

        _actionBar = activity.supportActionBar
        _appBarLayout = activity.findViewById(R.id.appbar_layout)
        _fab = activity.findViewById(R.id.fab)

        fab.hide()

        binding.apply {
            val scrollerChangeListener = ScrollerChangeListener()

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

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
        _fab = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_contact_pick, menu)
        super.onCreateMenu(menu,  menuInflater)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return  when (item.itemId) {
            else -> false
        }
    }
}