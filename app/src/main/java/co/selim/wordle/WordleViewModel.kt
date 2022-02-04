package co.selim.wordle

import androidx.lifecycle.ViewModel
import co.selim.wordle.state.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class WordleViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        GameState("Snake", "", emptyList())
    )

    val state: StateFlow<GameState> = _state

    fun onInputChanged(newInput: String) {
        val sanitizedInput = newInput.replace("[^a-zA-Z]".toRegex(), "")
            .uppercase()
            .take(5)

        _state.update {
            it.copy(input = sanitizedInput)
        }
    }

    fun submitWord() {
        _state.update {
            it.copy(guesses = it.guesses + it.input, input = "")
        }
    }
}