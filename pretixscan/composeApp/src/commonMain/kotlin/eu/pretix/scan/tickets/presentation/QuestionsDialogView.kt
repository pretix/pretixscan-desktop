package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.*
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.scan.tickets.data.ResultStateData
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
    val uiBlinkSpecialTickets by viewModel.uiBlinkSpecialTickets.collectAsState()
    val showNames by viewModel.showNames.collectAsState()
    val state = rememberLazyListState()
    LaunchedEffect(data) {
        viewModel.buildQuestionsForm(data)
        viewModel.applyUiSettings()
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

                        Column(horizontalAlignment = Alignment.Start) {
                            Row {
                                if (showNames && !data.attendeeName.isNullOrBlank()) {
                                    Text(data.attendeeName, style = MaterialTheme.typography.headlineSmall)
                                }
                            }


                            Row {
                                Text(data.ticketAndVariationName ?: "")

                                Spacer(modifier = Modifier.weight(1.0f))

                                // allow selection of the value with the mouse e.g. to copy-paste into pretix.eu
                                SelectionContainer {
                                    Text(data.orderCodeAndPositionId ?: "")
                                }
                            }

                        }
                    }

                    if (data.attention) {
                        AttentionTicketBar(
                            Modifier.blinking(
                                alphaEnabled = uiBlinkSpecialTickets
                            )
                        )
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
                                maxLines = 1,
                                required = field.required
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
                                maxLines = 1,
                                required = field.required
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
                                    label = field.label,
                                    required = field.required
                                )
                            }
                        }

                        QuestionType.B -> {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                RequiredTextLabel(
                                    label = field.label,
                                    required = field.required,
                                    fontWeight = FontWeight.SemiBold
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
                                RequiredTextLabel(
                                    label = field.label,
                                    required = field.required,
                                    fontWeight = FontWeight.SemiBold
                                )
                                FieldSpinner(
                                    selectedValue = field.value,
                                    availableOptions = field.keyValueOptions!!,
                                    onSelect = { selectedOption ->
                                        viewModel.updateAnswer(field.id, selectedOption?.value)
                                    }
                                )
                            }
                        }

                        QuestionType.M -> {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                RequiredTextLabel(
                                    label = field.label,
                                    required = field.required,
                                    fontWeight = FontWeight.SemiBold
                                )
                                field.keyValueOptions?.forEach { selectable ->
                                    QuestionCheckbox(
                                        label = selectable.label,
                                        checked = field.values?.contains(selectable.value) == true,
                                        onSelect = { isChecked ->
                                            viewModel.updateChoiceAnswer(
                                                field.id,
                                                selectable.value,
                                                isChecked == "True"
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        QuestionType.F -> {
                            FiledFileUpload(
                                label = field.label,
                                required = field.required,
                                validation = field.validation,
                                selectedFilePath = field.value,
                                onSelectFile = { viewModel.showModal(field) },
                                onDeleteFile = { viewModel.updateAnswer(field.id, null) },
                                imageLoader = viewModel.imageLoader
                            )
                        }

                        QuestionType.D -> {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                RequiredTextLabel(
                                    label = field.label,
                                    required = field.required,
                                    fontWeight = FontWeight.SemiBold
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
                                RequiredTextLabel(
                                    label = field.label,
                                    required = field.required,
                                    fontWeight = FontWeight.SemiBold
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
                                RequiredTextLabel(
                                    label = field.label,
                                    required = field.required,
                                    fontWeight = FontWeight.SemiBold
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
                                RequiredTextLabel(
                                    label = field.label,
                                    required = field.required,
                                    fontWeight = FontWeight.SemiBold
                                )
                                FieldSpinner(
                                    selectedValue = field.value,
                                    availableOptions = field.keyValueOptions!!.map { selectableValue ->
                                        selectableValue.copy(
                                            content = {
                                                // Custom rendering for dropdown items
                                                FieldSpinnerItem(selectableValue.label)
                                            }
                                        )
                                    },
                                    onSelect = { selectedOption ->
                                        viewModel.updateAnswer(field.id, selectedOption?.value)
                                    }
                                )
                            }

                        }

                        QuestionType.TEL -> {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                RequiredTextLabel(
                                    label = field.label,
                                    required = field.required,
                                    fontWeight = FontWeight.SemiBold
                                )
                                QuestionPhoneNumber(
                                    selectedValue = field.value,
                                    uiExtra = field.uiExtra,
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

        DialogBottomBar(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            cancelLabel = stringResource(Res.string.cancel),
            primaryLabel = stringResource(Res.string.cont),
            onCancel = onCancel,
            onPrimary = {
                if (viewModel.validateForConfirm()) {
                    onConfirm(viewModel.getCurrentAnswers(data))
                }
            }
        )
    }

    if (modalQuestion != null && modalQuestion?.fieldType == QuestionType.F) {
        QuestionPhoto(onDismiss = { viewModel.dismissModal(it) })
    }
}