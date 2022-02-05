package co.selim.wordle.state

sealed interface GameState {
    val word: String
    val guesses: List<String>

    data class InProgress(
        val input: String,
        override val word: String,
        override val guesses: List<String>
    ) : GameState

    data class Completed(
        override val word: String,
        override val guesses: List<String>
    ) : GameState
}
