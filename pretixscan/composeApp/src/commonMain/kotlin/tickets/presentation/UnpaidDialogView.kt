package tickets.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import main.presentation.selectlist.SelectCheckInList
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import pretixscan.composeapp.generated.resources.*
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.dialog_unpaid_title
import pretixscan.composeapp.generated.resources.ok
import tickets.data.ResultStateData

@Preview
@Composable
fun UnpaidDialogView(
    modifier: Modifier = Modifier,
    data: ResultStateData,
    onCheckInAnyway: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    Column(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(Res.string.dialog_unpaid_title))
        Text(stringResource(Res.string.dialog_unpaid_text))

        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onCancel) {
                Text(stringResource(Res.string.cancel))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { onCheckInAnyway() }
            ) {
                Text(stringResource(Res.string.dialog_unpaid_retry))
            }
        }
    }
}