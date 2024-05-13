package me.chamada.ft_hangouts.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import me.chamada.ft_hangouts.R

class DeleteDialogFragment(private val listener: OnConfirmListener): DialogFragment() {
    interface OnConfirmListener {
        fun onConfirmDelete()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return requireActivity().let {
            val builder = AlertDialog.Builder(it)

            builder
                .setTitle(getString(R.string.action_delete))
                .setMessage(getString(R.string.prompt_confirm_delete))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    listener.onConfirmDelete()
                    dismiss()
                }
                .setNegativeButton(getString(R.string.no)) { _, _ ->
                    dismiss()
                }

            builder.create()
        }
    }
}