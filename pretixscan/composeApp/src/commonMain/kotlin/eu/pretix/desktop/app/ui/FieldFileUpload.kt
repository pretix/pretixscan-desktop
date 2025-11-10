package eu.pretix.desktop.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.pretix.scan.tickets.presentation.QuestionImagePreview
import eu.pretix.scan.tickets.utils.ImageLoader
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.delete_photo
import pretixscan.composeapp.generated.resources.question_input_invalid
import pretixscan.composeapp.generated.resources.question_input_required
import pretixscan.composeapp.generated.resources.take_a_photo

@Composable
fun FiledFileUpload(
    label: String? = null,
    required: Boolean = false,
    validation: FieldValidationState?,
    selectedFilePath: String?,
    onSelectFile: () -> Unit = {},
    onDeleteFile: () -> Unit = {},
    imageLoader: ImageLoader? = null
) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        if (label != null) {
            RequiredTextLabel(label = label, required = required, fontWeight = FontWeight.SemiBold)
        }
        Row {
            Column(
                modifier = Modifier.weight(2f)
                    .padding(end = 16.dp)
            ) {
                Button(onClick = onSelectFile) {
                    Text(stringResource(Res.string.take_a_photo))
                }
                if (selectedFilePath != null) {
                    Button(
                        onClick = onDeleteFile) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(Res.string.delete_photo)
                        )
                        Text(stringResource(Res.string.delete_photo))
                    }
                }
            }

            if (selectedFilePath != null) {
                Box(modifier = Modifier.weight(1f)) {
                    QuestionImagePreview(
                        filePath = selectedFilePath,
                        imageLoader = imageLoader
                    )
                }
            }
        }

        if (validation != null) {
            when (validation) {
                FieldValidationState.INVALID -> {
                    Text(
                        stringResource(Res.string.question_input_invalid),
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }

                FieldValidationState.MISSING -> {
                    Text(
                        stringResource(Res.string.question_input_required),
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
            }
        }
    }
}