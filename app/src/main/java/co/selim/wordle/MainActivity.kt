package co.selim.wordle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.selim.wordle.state.Key
import co.selim.wordle.state.Tile
import co.selim.wordle.state.UiState
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
                    restart = { viewModel.restart() },
                )
            }
        }
    }
}

@Composable
private fun WordleContent(
    state: UiState,
    restart: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(2.dp)) {
            state.outcome?.let {
                Text(text = it)
            }
            if (state.showRestartButton) {
                Button(onClick = restart) {
                    Text(text = "Restart")
                }
            }
            Board(state)
            Keyboard(state.keyboard)
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun Board(state: UiState) {
    LazyVerticalGrid(cells = GridCells.Fixed(state.wordLength)) {
        state.tiles.forEach { tile ->
            item {
                Tile(tile)
            }
        }
    }
}

@Composable
private fun Tile(tile: Tile) {
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

@Composable
private fun Keyboard(keyboard: List<List<Key>>) {
    Column {
        keyboard.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                row.forEach {
                    Key(it)
                }
            }
        }
    }
}

@Composable
private fun RowScope.Key(key: Key) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable(enabled = key.onClick != null) {
                key.onClick?.invoke()
            }
            .background(key.color, shape = RoundedCornerShape(10))
    ) {
        Text(text = key.character.toString(), fontWeight = key.weight)
    }
}

@Composable
@Preview
private fun DefaultPreview() {
    WordleTheme {
        WordleContent(
            state = UiState.GameOver("You are decent", emptyList(), 5, emptyList()),
            restart = {},
        )
    }
}
