package me.chamada.ft_hangouts.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.chamada.ft_hangouts.R
import me.chamada.ft_hangouts.databinding.FragmentContactEditBinding


class EditFragment : Fragment() {
    private val viewModel: ContactViewModel by activityViewModels()

    private var _binding: FragmentContactEditBinding? = null
    private var _fab: FloatingActionButton? = null

    // These properties are only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val fab get() = _fab!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activity = requireActivity()

        _binding = FragmentContactEditBinding.inflate(inflater, container, false)
        _fab = activity.findViewById(R.id.fab)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.current.observe(viewLifecycleOwner) { contact ->
            binding.apply {
                editName.setText(contact.name)
                editPhoneNumber.setText(contact.phoneNumber)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        fab.setImageResource(R.drawable.ic_action_save)
        fab.setOnClickListener {
            if (binding.editName.text.isNotBlank() || binding.editPhoneNumber.text.isNotBlank()) {
                commit()
                findNavController().navigateUp()
            }
            else {
                binding.editName.error = getString(R.string.contact_blank_error)
            }
        }
        fab.show()
    }

    override fun onStop() {
        fab.setOnClickListener(null)
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun commit() {
        viewModel.current.value?.let {original ->
            val contact = original.copy(
                name = binding.editName.text.toString().trim(),
                phoneNumber = binding.editPhoneNumber.text.toString().trim()
            )

            if (contact.id == 0L)
                viewModel.insert(contact)
            else {
                viewModel.update(contact)
            }
        }
    }
}