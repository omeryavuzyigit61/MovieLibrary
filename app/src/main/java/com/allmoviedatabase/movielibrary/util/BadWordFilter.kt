package com.allmoviedatabase.movielibrary.util

import java.util.Locale

object BadWordFilter {
    // Buraya istemediğin kelimeleri küçük harfle yaz
    private val BAD_WORDS = listOf(
        "amk", "piç", "oç", "gerizekalı", "sik"
    )

    fun containsBadWord(text: String): Boolean {
        val lowerText = text.lowercase(Locale("tr"))
        return BAD_WORDS.any { lowerText.contains(it) }
    }
}