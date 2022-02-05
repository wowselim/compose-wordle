package co.selim.wordle

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.selim.wordle.state.GameState
import co.selim.wordle.state.Tile
import co.selim.wordle.state.UiState
import kotlinx.coroutines.flow.*

class WordleViewModel : ViewModel() {
    private val _state = MutableStateFlow<GameState>(
        GameState.InProgress("", "SNAKE", emptyList())
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
            .take(5)

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
                        currentState.input.length != 5 -> {
                            // Invalid input length
                            currentState
                        }
                        currentState.input == currentState.word -> {
                            // Correct guess
                            GameState.Completed(currentState.word, guesses)
                        }
                        guesses.size == 6 -> {
                            // Out of guesses
                            GameState.Completed(currentState.word, guesses)
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
        _state.value = GameState.InProgress("", "BARBS", emptyList())
    }
}

private fun GameState.toUiState(): UiState {
    return if (this is GameState.InProgress) {
        UiState.InProgress(input, createTiles())
    } else {
        val outcome = if (word in guesses) {
            "You won!"
        } else {
            "You suck!"
        }
        UiState.GameOver(outcome, createTiles())
    }
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
    val remainingTileCount = (6 * 5) - guessTiles.size - inputTiles.size
    val remainingTiles = buildList {
        repeat(remainingTileCount) {
            add(Tile(' ', Color.Transparent))
        }
    }
    return guessTiles + inputTiles + remainingTiles
}
