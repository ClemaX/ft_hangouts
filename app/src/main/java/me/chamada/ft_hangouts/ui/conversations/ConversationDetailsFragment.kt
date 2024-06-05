package me.chamada.ft_hangouts.ui.conversations

import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Bundle
import android.provider.Telephony
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.chamada.ft_hangouts.R
import me.chamada.ft_hangouts.adapters.SmsCursorAdapter
import me.chamada.ft_hangouts.data.model.conversation.ConversationWithContact
import me.chamada.ft_hangouts.databinding.FragmentConversationDetailsBinding
import me.chamada.ft_hangouts.ui.DeleteDialogFragment

class ConversationDetailsFragment : Fragment(), MenuProvider, DeleteDialogFragment.OnConfirmListener {
    private val viewModel: ConversationViewModel by activityViewModels()
    private var conversation: ConversationWithContact? = null

    private val deleteDialogFragment = DeleteDialogFragment(this)
    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        binding.submitButton.isEnabled = results[Manifest.permission.SEND_SMS] == true
        if (!binding.submitButton.isEnabled) {
            binding.messageInput.error = getString(R.string.permission_not_granted_send_sms)
        }
    }


    private var _smsContentObserver: SmsContentObserver? = null
    private var _adapter: SmsCursorAdapter? = null
    private var _binding: FragmentConversationDetailsBinding? = null
    private var _navbar: BottomNavigationView? = null
    private var _fab: FloatingActionButton? = null

    // These properties are only valid between onCreateView and onDestroyView.
    private val smsContentObserver get() = _smsContentObserver!!
    private val adapter get() = _adapter!!
    private val binding get() = _binding!!
    private val navbar get() = _navbar!!
    private val fab get() = _fab!!

    companion object {
        private const val TAG = "ConversationDetails"
        private val SMS_PERMISSIONS = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
        )
    }

    inner class SmsContentObserver(private val activity: AppCompatActivity) : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) = activity.runOnUiThread {
            adapter.notifySmsDataChanged()
        }
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

        _smsContentObserver = SmsContentObserver(activity)
        _adapter = SmsCursorAdapter(activity)

        _binding = FragmentConversationDetailsBinding.inflate(inflater, container, false)
        _navbar = activity.findViewById(R.id.navbar)
        _fab = activity.findViewById(R.id.fab)

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
                                println("Sent message successfully")
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

        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                val lastVisiblePosition = (binding.recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()

                if (lastVisiblePosition == -1 || (adapter.itemCount - lastVisiblePosition) <= 2) {
                    binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
                }
            }
        })

        viewModel.current.observe(viewLifecycleOwner) { conversation ->
            this.conversation = conversation

            binding.apply {
                submitButton.isEnabled = true
            }

            conversation.apply {
                adapter.setPhoneNumber(interlocutorPhoneNumber)

                activity.supportActionBar?.title = contactName ?:
                        interlocutorPhoneNumber
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val activity = requireActivity() as AppCompatActivity

        activity.contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI,
            true, smsContentObserver)
    }

    override fun onPause() {
        super.onPause()

        val activity = requireActivity() as AppCompatActivity

        activity.contentResolver.unregisterContentObserver(smsContentObserver)
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