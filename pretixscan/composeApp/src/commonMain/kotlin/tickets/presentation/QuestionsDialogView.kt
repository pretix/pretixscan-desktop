package tickets.presentation

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
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
import app.ui.CustomColor
import app.ui.FieldSpinner
import app.ui.asColor
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.db.Answer
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.*
import tickets.data.ResultStateData
import webcam.presentation.WebCam


@Preview
@Composable
fun QuestionsDialogView(
    modifier: Modifier = Modifier,
    data: ResultStateData,
    onConfirm: (List<Answer>) -> Unit,
    onCancel: () -> Unit
) {
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
            contentPadding = PaddingValues(bottom = 72.dp),
            state = state
        ) {
            item {
                Column(
                    modifier = Modifier
                        .background(CustomColor.BrandOrange.asColor()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.padding(PaddingValues(16.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(end = 16.dp))
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
                Box(modifier = Modifier.padding(PaddingValues(16.dp))) {
                    when (field.fieldType) {
                        QuestionType.N -> {
                            TextField(
                                value = field.value ?: "",
                                onValueChange = { newValue ->
                                    viewModel.updateAnswer(field.id, newValue)
                                },
                                label = { Text(field.label) },
                                singleLine = true
                            )
                        }

                        QuestionType.EMAIL,
                        QuestionType.S -> {
                            TextField(
                                value = field.value ?: "",
                                onValueChange = { viewModel.updateAnswer(field.id, it) },
                                label = { Text(field.label) },
                                singleLine = true
                            )
                        }

                        QuestionType.T -> {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    field.label
                                )
                                TextField(
                                    value = field.value ?: "",
                                    onValueChange = { viewModel.updateAnswer(field.id, it) },
                                    label = { Text(field.label) },
                                    maxLines = 2
                                )
                            }
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
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    field.label
                                )
                                FieldSpinner(
                                    selectedValue = field.value,
                                    availableOptions = field.keyValueOptions!!,
                                    onSelect = {
                                        viewModel.updateAnswer(field.id, it?.value)
                                    }
                                )
                            }
                        }

                        QuestionType.M -> {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    field.label
                                )
                                field.keyValueOptions?.forEach { option ->
                                    QuestionCheckbox(
                                        label = option.key,
                                        checked = field.values?.contains(option.value) ?: false,
                                        onSelect = {
                                            viewModel.updateChoiceAnswer(field.id, option.value, it == "True")
                                        })
                                }
                            }
                        }

                        QuestionType.F -> {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    field.label
                                )
                                Row {
                                    Column(
                                        modifier = Modifier.weight(2f)
                                            .padding(end = 16.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.showModal(field)
                                            }) {
                                            Text(stringResource(Res.string.take_a_photo))
                                        }
                                        if (field.value != null) {
                                            Button(
                                                onClick = {
                                                }) {
                                                Icon(
                                                    Icons.Filled.Delete,
                                                    contentDescription = stringResource(Res.string.delete_photo)
                                                )
                                                Text(stringResource(Res.string.delete_photo))
                                            }
                                        }
                                    }

                                    if (field.value != null) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            QuestionImagePreview(filePath = field.value!!)
                                        }
                                    }
                                }
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

                        QuestionType.H -> {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    field.label
                                )
                                QuestionTimePicker(
                                    value = field.value,
                                    onUpdate = {
                                        viewModel.updateAnswer(field.id, it)
                                    },
                                    label = field.label
                                )
                            }
                        }
                        QuestionType.W -> {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    field.label
                                )
                                QuestionDateTimePicker(
                                    minDate = field.dateConfig?.minDate,
                                    maxDate = field.dateConfig?.maxDate,
                                    value = field.value,
                                    onUpdate = {
                                        viewModel.updateAnswer(field.id, it)
                                    }
                                )
                            }
                        }

                        QuestionType.CC -> {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    field.label
                                )

                                FieldSpinner(
                                    selectedValue = field.value,
                                    availableOptions = field.keyValueOptions!!,
                                    onSelect = {
                                        viewModel.updateAnswer(field.id, it?.value)
                                    }
                                )
                            }

                        }

                        QuestionType.TEL -> {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    field.label
                                )

                                QuestionPhoneNumber(
                                    selectedValue = field.value,
                                    onSelect = {
                                        viewModel.updateAnswer(field.id, it)
                                    }
                                )
                            }
                        }
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

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CustomColor.White.asColor())
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onCancel) {
                    Text(stringResource(Res.string.cancel))
                }
                Spacer(modifier = Modifier.weight(1.0f))
                Button(
                    onClick = {
                        if (viewModel.validateForConfirm()) {
                            onConfirm(viewModel.getCurrentAnswers(data))
                        }
                    }
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