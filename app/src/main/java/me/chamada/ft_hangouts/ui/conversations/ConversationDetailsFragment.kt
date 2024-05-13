package me.chamada.ft_hangouts.ui.conversations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.chamada.ft_hangouts.R
import me.chamada.ft_hangouts.databinding.FragmentConversationDetailsBinding
import me.chamada.ft_hangouts.ui.DeleteDialogFragment

class ConversationDetailsFragment : Fragment(), MenuProvider, DeleteDialogFragment.OnConfirmListener {
    private val viewModel: ConversationViewModel by activityViewModels()
    private var conversationId: Long? = null

    private val deleteDialogFragment = DeleteDialogFragment(this)

    private var _binding: FragmentConversationDetailsBinding? = null
    private var _navbar: BottomNavigationView? = null
    private var _fab: FloatingActionButton? = null

    // These properties are only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val navbar get() = _navbar!!
    private val fab get() = _fab!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activity = requireActivity() as AppCompatActivity

        activity.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        _binding = FragmentConversationDetailsBinding.inflate(inflater, container, false)
        _navbar = activity.findViewById(R.id.navbar)
        _fab = activity.findViewById(R.id.fab)

        binding.apply {
            submitButton.setOnClickListener {
                println("TODO: Submit ${messageInput.text}")
                messageInput.text.clear()
            }
        }

        viewModel.current.observe(viewLifecycleOwner) { conversation ->
            conversationId = conversation.conversation.id

            activity.supportActionBar?.title = conversation.contactName ?: conversation.interlocutorPhoneNumber
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fab.hide()
        navbar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()

        navbar.visibility = View.VISIBLE

        _binding = null
        _navbar = null
        _fab = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_conversation_details, menu)
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
        conversationId?.let {
            findNavController().navigateUp()
            viewModel.select(0)
            viewModel.delete(it)
        }
    }
}