package tickets.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import tickets.data.ResultStateData

@Preview
@Composable
fun QuestionsDialogView(modifier: Modifier = Modifier, data: ResultStateData) {

    Text("Questions dialog")
}