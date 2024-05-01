package me.chamada.ft_hangouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import me.chamada.ft_hangouts.databinding.FragmentContactEditBinding


class EditFragment : Fragment() {
    private var _contact: Contact? = null
    private var _binding: FragmentContactEditBinding? = null

    // These properties are only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private var contact
        get() = _contact!!
        set(value) {
            _contact = value
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = (requireContext().applicationContext as ContactApplication).repository

        val viewModel: ContactViewModel by activityViewModels {
            ContactViewModel.Factory(repository)
        }

        val navController = findNavController()

        _contact = viewModel.current

        if (_contact == null)
            _contact = Contact()

        binding.editName.setText(contact.name)
        binding.editPhoneNumber.setText(contact.phoneNumber)

        binding.buttonCancel.setOnClickListener {
            navController.popBackStack()
        }

        binding.buttonDone.setOnClickListener {
            contact = contact.copy(
                name = binding.editName.text.toString().trim(),
                phoneNumber = binding.editPhoneNumber.text.toString().trim()
            )

            if (contact.id == 0)
                viewModel.insert(contact)
            else if (contact != viewModel.current)
                viewModel.update(contact)

            navController.navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}