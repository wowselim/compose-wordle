package co.selim.wordle

import androidx.lifecycle.ViewModel
import co.selim.wordle.state.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class WordleViewModel : ViewModel() {
    private val _state = MutableStateFlow<GameState>(
        GameState.InProgress("", "SNAKE", emptyList())
    )

    val state: StateFlow<GameState> = _state

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