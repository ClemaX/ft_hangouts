package me.chamada.ft_hangouts.ui.conversations

import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.chamada.ft_hangouts.R
import me.chamada.ft_hangouts.adapters.MessageListAdapter
import me.chamada.ft_hangouts.data.model.conversation.ConversationWithContact
import me.chamada.ft_hangouts.databinding.FragmentConversationDetailsBinding
import me.chamada.ft_hangouts.ui.DeleteDialogFragment

class ConversationDetailsFragment : Fragment(), MenuProvider, DeleteDialogFragment.OnConfirmListener {
    private val viewModel: ConversationViewModel by activityViewModels()
    private var conversation: ConversationWithContact? = null

    private val adapter = MessageListAdapter()

    private val deleteDialogFragment = DeleteDialogFragment(this)
    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        binding.submitButton.isEnabled = results[Manifest.permission.SEND_SMS] == true
        if (!binding.submitButton.isEnabled) {
            binding.messageInput.error = getString(R.string.permission_not_granted_send_sms)
        }
    }

    /// Indicates if the RecyclerView was scrolled to bottom before a data update
    private var wasScrolledToBottom = true

    private var _binding: FragmentConversationDetailsBinding? = null
    private var _navbar: BottomNavigationView? = null
    private var _fab: FloatingActionButton? = null

    // These properties are only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val navbar get() = _navbar!!
    private val fab get() = _fab!!

    companion object {
        private const val TAG = "ConversationDetails"
        private val SMS_PERMISSIONS = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activity = requireActivity() as AppCompatActivity

        val smsManager = ActivityCompat.getSystemService(activity, SmsManager::class.java)?:
            throw UnsupportedOperationException()

        activity.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        _binding = FragmentConversationDetailsBinding.inflate(inflater, container, false)
        _navbar = activity.findViewById(R.id.navbar)
        _fab = activity.findViewById(R.id.fab)

        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (wasScrolledToBottom && itemCount != 0) {
                    binding.recyclerView.smoothScrollToPosition(positionStart + itemCount - 1)
                }
            }
        })

        binding.apply {
            recyclerView.adapter = adapter

            messageInput.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
                { }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    submitButton.isEnabled = s?.isNotEmpty()?: false
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            submitButton.isEnabled = false

            submitButton.setOnClickListener {
                conversation?.let { conversation ->
                    submitButton.isEnabled = false

                    when (ActivityCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS)) {
                        PackageManager.PERMISSION_GRANTED -> {
                            val destinationAddress = conversation.interlocutorPhoneNumber
                            val scAddress: String? = null

                            val content: String = messageInput.text.toString()

                            val sentIntent: PendingIntent? = null
                            val deliveryIntent: PendingIntent? = null

                            println("Sending '$content' to '$destinationAddress' using '$scAddress'...")
                            try {
                                smsManager.sendTextMessage(destinationAddress, scAddress, content,
                                    sentIntent, deliveryIntent)
                            } catch (e: Exception) {
                                Log.e(TAG, "Could not send SMS: ${e.message}")
                            } finally {
                                messageInput.text.clear()
                                println("Inserting message to conversation ${conversation.conversation.id}")
                                viewModel.insertMessage(conversation.conversation.id, content)
                            }

                            submitButton.isEnabled = true
                        }
                        else -> {
                            println("Requesting SMS permissions...")
                            requestPermissions.launch(SMS_PERMISSIONS)
                        }
                    }
                }
            }
        }

        viewModel.current.observe(viewLifecycleOwner) { conversation ->
            this.conversation = conversation

            binding.apply {
                submitButton.isEnabled = true
            }

            activity.supportActionBar?.title = conversation.contactName ?:
                conversation.interlocutorPhoneNumber
        }

        viewModel.currentMessages.observe(viewLifecycleOwner) { messages ->
            wasScrolledToBottom = !binding.recyclerView.canScrollVertically(1)

            adapter.submitList(messages)
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
        conversation?.conversation?.id?.let {
            findNavController().navigateUp()
            viewModel.select(0)
            viewModel.delete(it)
        }
    }
}