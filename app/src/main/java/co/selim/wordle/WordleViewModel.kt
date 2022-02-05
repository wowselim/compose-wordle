package co.selim.wordle

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.selim.wordle.state.GameState
import co.selim.wordle.state.Key
import co.selim.wordle.state.Tile
import co.selim.wordle.state.UiState
import kotlinx.coroutines.flow.*

class WordleViewModel : ViewModel() {
    private val _state = MutableStateFlow<GameState>(
        GameState.InProgress("", "TREE", emptyList(), maxGuesses = 5)
    )

    val state: StateFlow<UiState> = _state
        .map { gameState ->
            gameState.toUiState()
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = _state.value.toUiState()
        )

    fun onInputChanged(newInput: String) {
        val sanitizedInput = newInput.replace("[^a-zA-Z]".toRegex(), "")
            .uppercase()
            .take(_state.value.wordLength)

        _state.update { currentState ->
            when (currentState) {
                is GameState.InProgress -> currentState.copy(input = sanitizedInput)
                is GameState.Completed -> currentState
            }
        }
    }

    fun submitWord() {
        _state.update { currentState ->
            when (currentState) {
                is GameState.InProgress -> {
                    val guesses = currentState.guesses + currentState.input
                    when {
                        currentState.input.length != currentState.wordLength -> {
                            // Invalid input length
                            currentState
                        }
                        currentState.input == currentState.word -> {
                            // Correct guess
                            GameState.Completed(currentState.word, guesses, currentState.maxGuesses)
                        }
                        guesses.size == currentState.maxGuesses -> {
                            // Out of guesses
                            GameState.Completed(currentState.word, guesses, currentState.maxGuesses)
                        }
                        else -> {
                            // Incorrect guess
                            currentState.copy(
                                guesses = guesses,
                                input = "",
                            )
                        }
                    }
                }
                is GameState.Completed -> currentState
            }
        }
    }

    fun restart() {
        _state.value =
            GameState.InProgress("", "BARBS", emptyList(), maxGuesses = _state.value.maxGuesses)
    }
}

private fun GameState.toUiState(): UiState {
    val keyboard = createKeyboard()
    return if (this is GameState.InProgress) {
        UiState.InProgress(input, createTiles(), wordLength, keyboard)
    } else {
        val outcome = if (word in guesses) {
            "You won!"
        } else {
            "You suck!"
        }
        UiState.GameOver(outcome, createTiles(), wordLength, keyboard)
    }
}

private fun GameState.createKeyboard(): List<List<Key>> {
    return listOf(
        "qwertyuiop".uppercase().map { it.toKey(word, guesses) },
        "asdfghjkl ".uppercase().map { it.toKey(word, guesses) },
        "⏎zxcvbnm⌫ ".uppercase().map { it.toKey(word, guesses) },
    )
}

private fun Char.toKey(word: String, guesses: List<String>): Key {

    val guessIndexes = guesses.flatMap { guess ->
        guess.flatMapIndexed { index, c ->
            if (c == this) {
                listOf(index)
            } else {
                emptyList()
            }
        }
    }

    val green = guessIndexes.any { index ->
        word[index] == this
    }

    val color = when {
        this == ' ' -> Color.Transparent
        green -> Color.Green
        guesses.any { guess -> this in guess } && this in word -> Color.Yellow
        guesses.any { guess -> this in guess } -> Color.DarkGray
        else -> Color.LightGray
    }
    return Key(this, color, {})
}

private fun GameState.createTiles(): List<Tile> {
    val guessTiles = guesses
        .flatMap {
            it.toCharArray().mapIndexed { i, c ->
                Tile(
                    c.uppercaseChar(),
                    if (word[i].equals(c, ignoreCase = true)) {
                        Color.Green
                    } else if (word.contains(c, ignoreCase = true)) {
                        Color.Yellow
                    } else {
                        Color.Black
                    }
                )
            }
        }
    val inputTiles = buildList {
        if (this@createTiles is GameState.InProgress) {
            input.forEach {
                add(Tile(it, Color.DarkGray))
            }
        }
    }
    val remainingTileCount = (maxGuesses * wordLength) - guessTiles.size - inputTiles.size
    val remainingTiles = buildList {
        repeat(remainingTileCount) {
            add(Tile(' ', Color.Transparent))
        }
    }
    return guessTiles + inputTiles + remainingTiles
}
