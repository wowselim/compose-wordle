package co.selim.wordle.state

sealed interface GameState {
    val word: String
    val guesses: List<String>
    val maxGuesses: Int
    val wordLength: Int get() = word.length

    data class InProgress(
        val input: String,
        override val word: String,
        override val guesses: List<String>,
        override val maxGuesses: Int,
    ) : GameState

    data class Completed(
        override val word: String,
        override val guesses: List<String>,
        override val maxGuesses: Int,
    ) : GameState
}
