package tickets.presentation


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ui.CustomColor
import app.ui.asColor
import eu.pretix.libpretixsync.db.QuestionOption
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.choose_option


@Composable
fun QuestionCheckbox(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onSelect: (String?) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { updatedChecked ->
                if (updatedChecked) {
                    onSelect("True")
                } else {
                    onSelect("False")
                }
            }
        )
        Text(
            label
        )
    }
}
