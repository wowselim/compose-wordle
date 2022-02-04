package co.selim.wordle.state

data class GameState(
    val word: String,
    val input: String,
    val guesses: List<String>,
)
