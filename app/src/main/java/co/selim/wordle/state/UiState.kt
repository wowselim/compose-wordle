package co.selim.wordle.state

import androidx.compose.ui.graphics.Color

sealed interface UiState {
    val input: String?
    val outcome: String?
    val showRestartButton: Boolean
    val tiles: List<Tile>

    data class InProgress(override val input: String, override val tiles: List<Tile>) : UiState {
        override val outcome: Nothing? = null
        override val showRestartButton = false
    }

    data class GameOver(override val outcome: String, override val tiles: List<Tile>) : UiState {
        override val input: Nothing? = null
        override val showRestartButton = true
    }
}

data class Tile(
    val character: Char,
    val color: Color,
)
