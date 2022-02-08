package co.selim.wordle.words

import android.content.Context
import androidx.annotation.RawRes
import co.selim.wordle.R
import java.util.zip.ZipInputStream

class WordRepository(context: Context) {
    private val riddleWords by lazy { readWordsFromFile(context, R.raw.riddle_words) }
    private val allWords by lazy { readWordsFromFile(context, R.raw.all_words) + riddleWords }

    private fun readWordsFromFile(context: Context, @RawRes riddleWords: Int): Set<String> {
        return ZipInputStream(context.resources.openRawResource(riddleWords))
            .use { stream ->
                stream.also(ZipInputStream::getNextEntry)
                    .bufferedReader()
                    .lineSequence()
                    .filter { it.isNotBlank() }
                    .map { it.trim().uppercase() }
                    .toSet()
            }
    }

    fun getWord(length: Int): String = riddleWords.random()

    fun isValid(word: String): Boolean = word in allWords
}
