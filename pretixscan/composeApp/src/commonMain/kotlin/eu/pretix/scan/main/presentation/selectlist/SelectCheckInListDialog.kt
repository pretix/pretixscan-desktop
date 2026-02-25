package eu.pretix.scan.main.presentation.selectlist


import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.DialogBottomBar
import eu.pretix.desktop.app.ui.FocusedRoundedDialog
import eu.pretix.desktop.app.ui.asColor
import eu.pretix.libpretixsync.sqldelight.CheckInList
import eu.pretix.scan.main.presentation.EventForSelection
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.action_label_back
import pretixscan.composeapp.generated.resources.operation_select_checkinlist
import pretixscan.composeapp.generated.resources.text_action_select

@Composable
@Preview
fun SelectCheckInListDialog(
    eventForSelection: EventForSelection,
    onSelectCheckInList: (CheckInList?) -> Unit
) {

    var selectedList by remember { mutableStateOf<CheckInList?>(null) }



    FocusedRoundedDialog(onDismiss = { onSelectCheckInList(null) }, content = {
        // header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CustomColor.BrandDark.asColor())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(eventForSelection.name, color = Color.White, fontWeight = FontWeight.Bold)
            Text(stringResource(Res.string.operation_select_checkinlist), color = Color.White)
        }

        // content list
        SelectCheckInList(
            selectedCheckInList = selectedList,
            onSelectCheckInList = { selectedList = it },
            eventSlug = eventForSelection.slug,
            subEventId = eventForSelection.subEventId,
        )

    }, bottomAccessory = {
        DialogBottomBar(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            primaryLabel = stringResource(Res.string.text_action_select),
            cancelLabel = stringResource(Res.string.action_label_back),
            onCancel = { onSelectCheckInList(null) },
            onPrimary = {
                onSelectCheckInList(selectedList)
            },
            primaryEnabled = (selectedList != null)
        )
    })
}