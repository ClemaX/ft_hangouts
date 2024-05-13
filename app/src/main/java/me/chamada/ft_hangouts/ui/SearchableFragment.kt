package me.chamada.ft_hangouts.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import me.chamada.ft_hangouts.R

abstract class SearchableFragment(@StringRes private val queryHintId: Int): Fragment(), MenuProvider {
    private var searchQuery: CharSequence? = null
    protected var filter: Filter? = null

    companion object {
        private const val KEY_SEARCH_QUERY = "searchQuery"
    }

    private inner class OnQueryTextListener :
        SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            searchQuery = newText
            filter?.filter(newText)

            return false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        searchQuery = savedInstanceState?.getCharSequence(KEY_SEARCH_QUERY)

        searchQuery?.let {
            filter?.filter(it)
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putCharSequence(KEY_SEARCH_QUERY, searchQuery)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        val searchMenuItem = menu.findItem(R.id.action_search)

        (searchMenuItem.actionView as SearchView?)?.apply {
            // Restore search query if it is set
            searchQuery?.let {
                isIconified = false
                setQuery(it, false)
            }

            // Instantiate query listener to update filter
            val queryListener = OnQueryTextListener()

            queryHint = resources.getString(queryHintId)
            setOnQueryTextListener(queryListener)
        }
    }
}