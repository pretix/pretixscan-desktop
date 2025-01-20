package tickets.presentation

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import eu.pretix.libpretixsync.check.QuestionType
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.*
import tickets.data.ResultStateData
import webcam.presentation.WebCam
import java.time.format.DateTimeFormatter


@Preview
@Composable
fun QuestionsDialogView(modifier: Modifier = Modifier, data: ResultStateData) {
    val viewModel = koinViewModel<QuestionsDialogViewModel>()
    val form by viewModel.form.collectAsState()
    val modalQuestion by viewModel.modalQuestion.collectAsState()

    val state = rememberLazyListState()
    LaunchedEffect(data) {
        viewModel.buildQuestionsForm(data)
    }

    Box {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            state = state
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
//                        TextField(
//                            value = field.value ?: "",
//                            onValueChange = { viewModel.updateAnswer(field.id, it) },
//                            label = { Text(field.label) },
//                            singleLine = false,
//                            maxLines = 2
//                        )
                        QuestionTimePicker(
                            value = field.value,
                            onUpdate = {
                                viewModel.updateAnswer(field.id, it)
                            }
                        )
                    }

                    QuestionType.B -> {
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                field.label
                            )
                            QuestionCheckbox(
                                label = stringResource(Res.string.yes),
                                checked = "True" == field.value,
                                onSelect = {
                                    viewModel.updateAnswer(field.id, it)
                                })
                        }
                    }

                    QuestionType.C -> {
                        QuestionSpinner(
                            selectedValue = field.value,
                            availeOptions = field.availableOptions!!,
                            onSelect = {
                                viewModel.updateAnswer(field.id, it?.value)
                            }
                        )
                    }

                    QuestionType.M -> {
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                field.label
                            )
                            field.availableOptions?.forEach { option ->
                                QuestionCheckbox(
                                    label = option.value,
                                    checked = field.values?.contains(option.value) ?: false,
                                    onSelect = {
                                        viewModel.updateChoiceAnswer(field.id, option.value, it == "True")
                                    })
                            }
                        }
                    }

                    QuestionType.F -> {
                        Button(onClick = {
                            viewModel.showModal(field)
                        }) {
                            Text(stringResource(Res.string.take_a_photo))
                        }
                    }

                    QuestionType.D -> {
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                field.label
                            )
                            QuestionDatepicker(
                                minDate = field.dateConfig?.minDate,
                                maxDate = field.dateConfig?.maxDate,
                                value = field.value,
                                onUpdate = {
                                    viewModel.updateAnswer(field.id, it)
                                }
                            )
                        }
                    }

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

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = state
            )
        )
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