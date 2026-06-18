package com.stickerapk.data

object VowelUtils {
    private val VOWELS = setOf('a', 'e', 'i', 'o', 'u', 'y')

    const val MAX_DISTINCT_VOWELS = 6

    /** Counts distinct vowel letters in a word (a, e, i, o, u, y). */
    fun countDistinctVowels(word: String): Int =
        word.lowercase().filter { it in VOWELS }.toSet().size
}