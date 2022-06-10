package me.chamada.ft_hangouts

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

open class ContactUriMatcher(authority: String) : UriMatcher(UriMatcher.NO_MATCH) {
    init {
        super.addURI(authority, "/contacts", 1)
        super.addURI(authority, "/contacts/*", 1)
    }
}

class ContactProvider : ContentProvider() {
    companion object {
        const val PROVIDER_ID: String = "me.chamada.ft_hangouts.contacts"
        final var CONTENT_URI: Uri = Uri.parse("content://$PROVIDER_ID/contacts")
    }
    private var _db: SQLiteDatabase? = null
    companion object URIMatcher : ContactUriMatcher(ContactUriMatcher.PROVIDER_ID);

    // This property is only valid after onCreate
    private val db get() = _db!!

    override fun onCreate(): Boolean {
        _db = SQLiteDatabase.openOrCreateDatabase("contacts", null)

        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        TODO("Not yet implemented")
    }

    override fun getType(uri: Uri): String? {
        TODO("Not yet implemented")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        TODO("Not yet implemented")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("Not yet implemented")
    }


}