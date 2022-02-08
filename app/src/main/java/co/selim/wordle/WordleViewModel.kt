package co.selim.wordle

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
            gameState.toUiState(::onKeyPressed)
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = _state.value.toUiState(::onKeyPressed)
        )

    private fun onKeyPressed(key: Char) {
        when (key) {
            '⏎' -> submitWord()
            '⌫' -> onBackspace()
            else -> onLetterPressed(key)
        }
    }

    private fun onLetterPressed(key: Char) {
        _state.update { currentState ->
            when (currentState) {
                is GameState.InProgress -> {
                    val newInput = currentState.input + key
                    val sanitizedInput = newInput.replace("[^a-zA-Z]".toRegex(), "")
                        .uppercase()
                        .take(_state.value.wordLength)
                    currentState.copy(input = sanitizedInput)
                }
                is GameState.Completed -> currentState
            }
        }
    }

    private fun onBackspace() {
        _state.update { currentState ->
            when (currentState) {
                is GameState.InProgress -> {
                    val newInput = currentState.input.dropLast(1)
                    currentState.copy(input = newInput)
                }
                is GameState.Completed -> currentState
            }
        }
    }

    private fun submitWord() {
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

private fun GameState.toUiState(onKeyPressed: (Char) -> Unit): UiState {
    val keyboard = createKeyboard(onKeyPressed)
    return if (this is GameState.InProgress) {
        UiState.InProgress(createTiles(), wordLength, keyboard)
    } else {
        val outcome = if (word in guesses) {
            "You won!"
        } else {
            "You suck!"
        }
        UiState.GameOver(outcome, createTiles(), wordLength, keyboard)
    }
}

private fun GameState.createKeyboard(onKeyPressed: (Char) -> Unit): List<List<Key>> {
    return listOf(
        "qwertyuiop".uppercase().map { it.toKey(this, onKeyPressed) },
        "asdfghjkl ".uppercase().map { it.toKey(this, onKeyPressed) },
        "⏎zxcvbnm⌫ ".uppercase().map { it.toKey(this, onKeyPressed) },
    )
}

private fun Char.toKey(state: GameState, onKeyPressed: (Char) -> Unit): Key {

    val guessIndexes = state.guesses.flatMap { guess ->
        guess.flatMapIndexed { index, c ->
            if (c == this) {
                listOf(index)
            } else {
                emptyList()
            }
        }
    }

    val green = guessIndexes.any { index ->
        state.word[index] == this
    }

    val backgroundColor = when {
        this == ' ' -> Color.Transparent
        green -> Color.Green
        state.guesses.any { guess -> this in guess } && this in state.word -> Color.Yellow
        state.guesses.any { guess -> this in guess } -> Color.DarkGray
        else -> Color.LightGray
    }
    val hasReachedMaxLength = (state as? GameState.InProgress)?.input?.length == state.wordLength
    val isBlank = (state as? GameState.InProgress)?.input?.length == 0
    val enabled = when (this) {
        ' ' -> false
        '⏎' -> hasReachedMaxLength
        '⌫' -> !isBlank
        else -> !hasReachedMaxLength
    }
    val onClick = { onKeyPressed(this) }.takeIf { enabled && state is GameState.InProgress }
    val foregroundColor = if (onClick != null) Color.Black else Color.Black.copy(alpha = 0.2f)
    val weight = if (this == '⏎') FontWeight.Bold else FontWeight.Normal
    return Key(this, backgroundColor, foregroundColor, weight, onClick)
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
