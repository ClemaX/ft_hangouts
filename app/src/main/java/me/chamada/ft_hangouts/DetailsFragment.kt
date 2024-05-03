package me.chamada.ft_hangouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.chamada.ft_hangouts.databinding.FragmentContactDetailsBinding

class DetailsFragment : Fragment() {
    private val viewModel: ContactViewModel by activityViewModels()

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

        _binding = FragmentContactDetailsBinding.inflate(inflater, container, false)
        _fab = activity.findViewById(R.id.fab)

        viewModel.current.observe(viewLifecycleOwner) { contact ->
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
}