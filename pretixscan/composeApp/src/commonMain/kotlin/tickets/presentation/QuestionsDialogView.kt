package tickets.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.pretix.libpretixsync.check.QuestionType
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.*
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.cancel
import pretixscan.composeapp.generated.resources.cont
import pretixscan.composeapp.generated.resources.yes
import tickets.data.ResultStateData
import webcam.presentation.WebCam
import java.util.*

@Preview
@Composable
fun QuestionsDialogView(modifier: Modifier = Modifier, data: ResultStateData) {
    val viewModel = koinViewModel<QuestionsDialogViewModel>()
    val form by viewModel.form.collectAsState()

    val modalQuestion by viewModel.modalQuestion.collectAsState()

    LaunchedEffect(data) {
        viewModel.buildQuestionsForm(data)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(data.ticketAndVariationName ?: "")
                    if (!data.attendeeName.isNullOrBlank()) {
                        Text(data.attendeeName)
                    }

                    Spacer(modifier = Modifier.weight(1.0f))

                    Text(data.orderCodeAndPositionId ?: "")
                }
            }
        }
        items(form) { field ->

            when (field.fieldType) {
                QuestionType.N -> {
                    TextField(
                        value = field.value ?: "",
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                viewModel.updateAnswer(field.id, newValue)
                            }
                        },
                        label = { Text(field.label) },
                        singleLine = true
                    )
                }

                QuestionType.S -> {
                    TextField(
                        value = field.value ?: "",
                        onValueChange = { viewModel.updateAnswer(field.id, it) },
                        label = { Text(field.label) },
                        singleLine = true
                    )
                }

                QuestionType.T -> {
                    TextField(
                        value = field.value ?: "",
                        onValueChange = { viewModel.updateAnswer(field.id, it) },
                        label = { Text(field.label) },
                        singleLine = false,
                        maxLines = 2
                    )
                }

                QuestionType.B -> {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            field.label
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(Res.string.yes)
                            )
                            Checkbox(
                                checked = "True" == field.value,
                                onCheckedChange = { updatedChecked ->
                                    if (updatedChecked) {
                                        viewModel.updateAnswer(field.id, "True")
                                    } else {
                                        viewModel.updateAnswer(field.id, "False")
                                    }
                                }
                            )
                        }
                    }
                }

                QuestionType.C -> {}
                QuestionType.M -> {}
                QuestionType.F -> {
                    Button(onClick = {
                        viewModel.showModal(field)
                    }) {
                        Text(stringResource(Res.string.take_a_photo))
                    }
                }

                QuestionType.D -> {}
                QuestionType.H -> {}
                QuestionType.W -> {}
                QuestionType.CC -> {}
                QuestionType.TEL -> {}
                QuestionType.EMAIL -> {}
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {}) {
                    Text(stringResource(Res.string.cancel))
                }
                Spacer(modifier = Modifier.weight(1.0f))
                Button(
                    onClick = { viewModel.validateAndContinue() }
                ) {
                    Text(stringResource(Res.string.cont))
                }
            }
        }
    }

    if (modalQuestion != null && modalQuestion?.fieldType == QuestionType.F) {
        Dialog(
            onDismissRequest = { viewModel.dismissModal(null) },
            properties = DialogProperties(
                usePlatformDefaultWidth = false // Ensures the dialog can be full-window size
            ),
        ) {
            WebCam(onPhotoTaken = {
                viewModel.dismissModal(it)
            })
        }
    }
}

val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
val timeFormat = DateTimeFormatter.ofPattern("HH:mm")