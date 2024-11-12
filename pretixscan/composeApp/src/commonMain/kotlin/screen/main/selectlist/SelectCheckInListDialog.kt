package screen.main.selectlist


import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import eu.pretix.libpretixsync.sqldelight.CheckInList
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.ok
import pretixscan.composeapp.generated.resources.operation_select_checkinlist

@Composable
@Preview
fun SelectCheckInListDialog(
    onSelectCheckInList: (CheckInList?) -> Unit
) {
    var selectedList by remember { mutableStateOf<CheckInList?>(null) }

    Dialog(onDismissRequest = { onSelectCheckInList(null) }) {
        // Custom shape, background, and layout for the dialog
        Surface(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(Res.string.operation_select_checkinlist))
                SelectCheckInList(
                    selectedCheckInList = selectedList,
                    onSelectCheckInList = { selectedList = it },
                )

                Button(
                    onClick = { onSelectCheckInList(selectedList) },
                    modifier = Modifier.padding(top = 16.dp),
                    enabled = selectedList != null,
                ) {
                    Text(stringResource(Res.string.ok))
                }
            }
        }
    }
}