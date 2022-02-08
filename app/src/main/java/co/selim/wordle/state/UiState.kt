package co.selim.wordle.state

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

sealed interface UiState {
    val outcome: String?
    val showRestartButton: Boolean
    val tiles: List<Tile>
    val wordLength: Int
    val keyboard: List<List<Key>>

    data class InProgress(
        override val tiles: List<Tile>,
        override val wordLength: Int,
        override val keyboard: List<List<Key>>
    ) : UiState {
        override val outcome: Nothing? = null
        override val showRestartButton = false
    }

    data class GameOver(
        override val outcome: String,
        override val tiles: List<Tile>,
        override val wordLength: Int,
        override val keyboard: List<List<Key>>
    ) : UiState {
        override val showRestartButton = true
    }
}

data class Tile(
    val character: Char,
    val color: Color,
)

data class Key(
    val character: Char,
    val backgroundColor: Color,
    val foregroundColor: Color,
    val weight: FontWeight,
    val onClick: (() -> Unit)?,
)