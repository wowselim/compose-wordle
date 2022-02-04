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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import co.selim.wordle.state.GameState
import co.selim.wordle.ui.theme.WordleTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<WordleViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.state.collectAsState()
                    Column(modifier = Modifier.padding(2.dp)) {
                        BasicTextField(
                            value = state.input,
                            onValueChange = {
                                viewModel.onInputChanged(it)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = if (state.input.length == 5) ImeAction.Done else ImeAction.None),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    viewModel.submitWord()
                                }
                            ),
                            modifier = Modifier
                                .border(1.dp, Color.Black)
                                .fillMaxWidth()
                                .padding(4.dp),
                        )
                        Board(state)
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun DefaultPreview(state: GameState, onInputChanged: (String) -> Unit) {
    WordleTheme {

    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun Board(state: GameState) {
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
