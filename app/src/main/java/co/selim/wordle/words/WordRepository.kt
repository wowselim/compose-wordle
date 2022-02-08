package co.selim.wordle.words

import android.content.Context
import androidx.annotation.RawRes
import co.selim.wordle.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.zip.ZipInputStream

class WordRepository(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val riddleWords by lazy { readWordsFromFile(context, R.raw.riddle_words) }
    private val allWords by lazy { readWordsFromFile(context, R.raw.all_words) + riddleWords }

    private fun readWordsFromFile(context: Context, @RawRes riddleWords: Int): Set<String> {
        return context.resources.openRawResource(riddleWords).useUnzipped { files ->
            files.flatMap { stream ->
                stream.bufferedReader()
                    .lineSequence()
                    .filter { it.isNotBlank() }
                    .map { it.trim().uppercase() }
            }.toSet()
        }
    }

    suspend fun getWord(length: Int): String = withContext(dispatcher) {
        riddleWords.random()
    }

    suspend fun isValid(word: String): Boolean = withContext(dispatcher) {
        word in allWords
    }
}

private fun <T> InputStream.useUnzipped(block: (Sequence<InputStream>) -> T): T {
    val sequence = sequence {
        ZipInputStream(this@useUnzipped).use { zipStream ->
            var nextEntry = zipStream.nextEntry
            while (nextEntry != null) {
                if (!nextEntry.isDirectory) {
                    yield(zipStream)
                }
                nextEntry = zipStream.nextEntry
            }
        }
    }
    return block(sequence)
}
