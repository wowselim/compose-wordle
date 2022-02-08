package co.selim.wordle.words

import android.content.Context
import androidx.annotation.RawRes
import co.selim.wordle.R

class WordRepository(context: Context) {
    private val riddleWords by lazy { readWordsFromFile(context, R.raw.riddle_words) }
    private val allWords by lazy { readWordsFromFile(context, R.raw.all_words) + riddleWords }

    private fun readWordsFromFile(context: Context, @RawRes riddleWords: Int): Set<String> {
        return context.resources.openRawResource(riddleWords).reader().useLines { lines ->
            lines
                .filter { it.isNotBlank() }
                .map { it.trim().uppercase() }
                .toSet()
        }
    }

    fun getWord(length: Int): String = riddleWords.random()

    fun isValid(word: String): Boolean = word in allWords
}
