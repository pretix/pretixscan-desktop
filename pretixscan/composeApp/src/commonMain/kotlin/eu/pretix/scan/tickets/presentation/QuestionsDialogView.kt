package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import eu.pretix.desktop.app.ui.*
import eu.pretix.desktop.webcam.presentation.WebCam
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.scan.tickets.data.ResultStateData
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.*


@Preview
@Composable
fun QuestionsDialogView(
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

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .focusable(),
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

                        // allow selection of the value with the mouse e.g. to copy-paste into pretix.eu
                        SelectionContainer {
                            Text(data.orderCodeAndPositionId ?: "")
                        }
                    }

                    if (data.attention) {
                        Row(
                            modifier = Modifier
                                .background(CustomColor.BrandBlue.asColor())
                                .fillMaxWidth()
                                .padding(PaddingValues(16.dp)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(stringResource(Res.string.ticket_attention))
                            Image(
                                painter = painterResource(Res.drawable.ic_warning_white_24dp),
                                contentDescription = stringResource(Res.string.ticket_attention),
                            )
                        }
                    }
                }
            }
            items(form) { field ->
                Box(modifier = Modifier.padding(PaddingValues(16.dp))) {
                    when (field.fieldType) {
                        QuestionType.N -> {
                            FieldTextInput(
                                value = field.value ?: "",
                                onValueChange = { newValue ->
                                    viewModel.updateAnswer(field.id, newValue)
                                },
                                label = field.label,
                                maxLines = 1
                            )
                        }

                        QuestionType.EMAIL,
                        QuestionType.S -> {
                            FieldTextInput(
                                value = field.value ?: "",
                                onValueChange = { newValue ->
                                    viewModel.updateAnswer(field.id, newValue)
                                },
                                label = field.label,
                                maxLines = 1
                            )
                        }

                        QuestionType.T -> {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                FieldTextInput(
                                    value = field.value ?: "",
                                    onValueChange = { viewModel.updateAnswer(field.id, it) },
                                    maxLines = 2,
                                    label = field.label
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
                                    availableOptions = field.keyValueOptions!!.map {
                                        SelectableValue(
                                            it.value,
                                            it.key
                                        )
                                    },
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
                                        checked = field.values?.contains(option.value) == true,
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
                                    availableOptions = field.keyValueOptions!!.map {
                                        SelectableValue(
                                            it.value,
                                            it.key
                                        )
                                    },
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
                                    onSelect = { phone, country ->
                                        viewModel.updateAnswer(field.id, phone, country)
                                    },
                                    validation = field.validation
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