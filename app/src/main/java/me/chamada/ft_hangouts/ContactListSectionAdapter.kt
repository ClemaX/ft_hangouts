package me.chamada.ft_hangouts

import android.util.Log
import android.widget.SectionIndexer

class ContactListSectionAdapter(clickListener: OnContactClickListener?) : ContactListAdapter(clickListener), SectionIndexer {
    private var sections: HashMap<Char, Int> = HashMap();

    override fun onCurrentListChanged(
        previousList: List<Contact>,
        currentList: List<Contact>
    ) {
        if (previousList != currentList) {
            Log.d("Sections","Updating sections...")

            val it = currentList.toMutableList().listIterator()

            for ((position, item) in it.withIndex()) {
                val index: Char = item.name[0].uppercaseChar()

                // Add the section's position to the map
                if (!sections.containsKey(index))
                    sections[index] = position
            }
            Log.d("Sections", sections.toString());
        }
        super.onCurrentListChanged(previousList, currentList)
    }

    override fun getSections(): Array<Any> {
        return sections.keys.toTypedArray();
    }

    override fun getPositionForSection(index: Int): Int {
        if (index < sections.size) return sections[sections.keys.toCharArray()[index]]!!
        return 0
    }

    override fun getSectionForPosition(p0: Int): Int {
        TODO("Not yet implemented")
    }
}