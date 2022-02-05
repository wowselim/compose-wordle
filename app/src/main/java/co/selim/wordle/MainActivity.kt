package co.selim.wordle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.selim.wordle.state.GameState
import co.selim.wordle.ui.theme.WordleTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<WordleViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.state.collectAsState()
            WordleTheme {
                WordleContent(
                    state = state,
                    onValueChange = { viewModel.onInputChanged(it) },
                    submitWord = { viewModel.submitWord() },
                    restart = { viewModel.restart() },
                )
            }
        }
    }
}

@Composable
private fun WordleContent(
    state: GameState,
    onValueChange: (String) -> Unit,
    submitWord: KeyboardActionScope.() -> Unit,
    restart: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(2.dp)) {
            if (state is GameState.InProgress) {
                TextField(state.input, onValueChange, submitWord)
            } else {
                if (state.word in state.guesses) {
                    Text(text = "You won!")
                } else {
                    Text(text = "You suck!")
                }
                Button(onClick = restart) {
                    Text(text = "Restart")
                }
            }
            Board(state)
        }
    }
}

@Composable
private fun TextField(
    input: String,
    onValueChange: (String) -> Unit,
    submitWord: KeyboardActionScope.() -> Unit
) {
    BasicTextField(
        value = input,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = if (input.length == 5) ImeAction.Done else ImeAction.None),
        keyboardActions = KeyboardActions(
            onDone = submitWord
        ),
        modifier = Modifier
            .border(1.dp, Color.Black)
            .fillMaxWidth()
            .padding(4.dp),
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun Board(state: GameState) {
    data class Tile(
        val character: Char,
        val color: Color,
    )

    data class ViewState(val word: String, val tiles: List<Tile>)

    val viewState = ViewState(
        state.word.uppercase(),
        state.guesses
            .flatMap {
                it.toCharArray().mapIndexed { i, c ->
                    Tile(
                        c.uppercaseChar(),
                        if (state.word[i].equals(c, ignoreCase = true)) {
                            Color.Green
                        } else if (state.word.contains(c, ignoreCase = true)) {
                            Color.Yellow
                        } else {
                            Color.Black
                        }
                    )
                }
            }
                + buildList {
            repeat((6 - state.guesses.size) * 5) {
                add(Tile(' ', Color.Transparent))
            }
        }
    )

    LazyVerticalGrid(cells = GridCells.Fixed(5)) {
        viewState.tiles.forEach { tile ->
            item {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .requiredHeight(maxWidth)
                            .padding(2.dp)
                            .background(Color.Gray, shape = RoundedCornerShape(10))
                    ) {
                        Text(text = tile.character.toString(), color = tile.color)
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun DefaultPreview() {
    WordleTheme {
        WordleContent(
            state = GameState.InProgress("SNAKE", "seks", listOf("EKANS", "BLARF")),
            onValueChange = {},
            submitWord = {},
            restart = {},
        )
    }
}
