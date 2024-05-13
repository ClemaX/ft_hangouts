package me.chamada.ft_hangouts.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.chamada.ft_hangouts.R
import me.chamada.ft_hangouts.databinding.FragmentContactDetailsBinding
import me.chamada.ft_hangouts.ui.DeleteDialogFragment

class DetailsFragment : Fragment(), MenuProvider, DeleteDialogFragment.OnConfirmListener {
    private val viewModel: ContactViewModel by activityViewModels()
    private var contactId: Long? = null

    private val deleteDialogFragment = DeleteDialogFragment(this)

    private var _binding: FragmentContactDetailsBinding? = null
    private var _fab: FloatingActionButton? = null

    // These properties are only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val fab get() = _fab!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activity = requireActivity()

        activity.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        _binding = FragmentContactDetailsBinding.inflate(inflater, container, false)
        _fab = activity.findViewById(R.id.fab)

        viewModel.current.observe(viewLifecycleOwner) { contact ->
            contactId = contact.id

            binding.apply {
                name.text = contact.name
                phoneNumber.text = contact.phoneNumber
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        fab.setImageResource(R.drawable.ic_action_edit)
        fab.setOnClickListener {
            editContact()
        }
        fab.show()
    }

    override fun onStop() {
        fab.setOnClickListener(null)

        super.onStop()
    }

    private fun editContact() {
        val action = DetailsFragmentDirections.actionDetailsFragmentToEditFragment()

        findNavController().navigate(action)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_contact_details, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_delete -> {
                deleteDialogFragment
                    .show(requireActivity().supportFragmentManager, "DELETE_CONTACT_DIALOG")

                true
            }
            else -> false
        }
    }

    override fun onConfirmDelete() {
        contactId?.let {
            findNavController().navigateUp()
            viewModel.select(0)
            viewModel.delete(it)
        }
    }
}