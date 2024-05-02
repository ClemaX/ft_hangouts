package me.chamada.ft_hangouts

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()
        val fab = activity.findViewById<FloatingActionButton>(R.id.fab)

        fab.hide()
    }
}