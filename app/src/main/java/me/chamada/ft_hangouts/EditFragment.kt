package me.chamada.ft_hangouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.chamada.ft_hangouts.databinding.FragmentContactEditBinding


class EditFragment : Fragment() {
    private var _contact: Contact? = null
    private var _binding: FragmentContactEditBinding? = null
    private var _fab: FloatingActionButton? = null

    // These properties are only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val fab get() = _fab!!
    private var contact
        get() = _contact!!
        set(value) {
            _contact = value
        }

    private val viewModel: ContactViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activity = requireActivity()

        _binding = FragmentContactEditBinding.inflate(inflater, container, false)
        _fab = activity.findViewById<FloatingActionButton>(R.id.fab)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        _contact = viewModel.current ?: Contact()

        fab.setImageResource(R.drawable.baseline_save_24)
        fab.setOnClickListener {
            if (binding.editName.text.isNotBlank() || binding.editPhoneNumber.text.isNotBlank()) {
                commit()
                navController.navigateUp()
            }
            else {
                binding.editName.error = getString(R.string.contact_blank_error)
            }
        }
        fab.show()

        binding.apply {
            editName.setText(contact.name)
            editPhoneNumber.setText(contact.phoneNumber)
        }


        /*binding.buttonCancel.setOnClickListener {
            navController.popBackStack()
        }

        binding.buttonDone.setOnClickListener {
            commit()

        }*/
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
        contact = contact.copy(
            name = binding.editName.text.toString().trim(),
            phoneNumber = binding.editPhoneNumber.text.toString().trim()
        )

        if (contact.id == 0)
            viewModel.insert(contact)
        else if (contact != viewModel.current)
            viewModel.update(contact)
    }
}