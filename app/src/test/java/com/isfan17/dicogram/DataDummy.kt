package com.isfan17.dicogram

import com.isfan17.dicogram.data.model.Story

object DataDummy {

    fun generateDummyStory(): List<Story> {
        val items: MutableList<Story> = arrayListOf()
        for (i in 0..100) {
            val story = Story(
                i.toString(),
                "name + $i",
                "desc $i",
                "photoUrl $i",
                "12-12-20$i",
            )
            items.add(story)
        }
        return items
    }
}