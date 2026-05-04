package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.*
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.scan.tickets.data.ResultStateData
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.cancel
import pretixscan.composeapp.generated.resources.cont
import pretixscan.composeapp.generated.resources.question_input_date_out_of_range
import pretixscan.composeapp.generated.resources.question_input_date_too_early
import pretixscan.composeapp.generated.resources.question_input_date_too_late
import pretixscan.composeapp.generated.resources.question_input_number_out_of_range
import pretixscan.composeapp.generated.resources.question_input_number_too_high
import pretixscan.composeapp.generated.resources.question_input_number_too_low
import pretixscan.composeapp.generated.resources.yes


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
    val coroutineScope = rememberCoroutineScope()
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
            itemsIndexed(form) { index, field ->
                SelectListRow {
                    when (field.fieldType) {
                        QuestionType.N -> {
                            val rangeMessage = when {
                                field.numberMin != null && field.numberMax != null ->
                                    stringResource(Res.string.question_input_number_out_of_range, field.numberMin.formatNumber(), field.numberMax.formatNumber())
                                field.numberMin != null ->
                                    stringResource(Res.string.question_input_number_too_low, field.numberMin.formatNumber())
                                field.numberMax != null ->
                                    stringResource(Res.string.question_input_number_too_high, field.numberMax.formatNumber())
                                else -> null
                            }
                            FieldTextInput(
                                value = field.value ?: "",
                                onValueChange = { newValue ->
                                    viewModel.updateAnswer(field.id, newValue)
                                },
                                label = field.label,
                                maxLines = 1,
                                required = field.required,
                                validation = field.validation,
                                validationMessage = rangeMessage
                            )
                        }

                        QuestionType.EMAIL -> {
                            FieldTextInput(
                                value = field.value ?: "",
                                onValueChange = { newValue ->
                                    viewModel.updateAnswer(field.id, newValue)
                                },
                                label = field.label,
                                maxLines = 1,
                                required = field.required,
                                validation = field.validation
                            )
                        }

                        QuestionType.S -> {
                            FieldTextInput(
                                value = field.value ?: "",
                                onValueChange = { newValue ->
                                    viewModel.updateAnswer(field.id, newValue)
                                },
                                label = field.label,
                                maxLines = 1,
                                required = field.required,
                                validation = field.validation,
                                maxLength = field.maxLength
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
                                    minLines = 2,
                                    label = field.label,
                                    required = field.required,
                                    validation = field.validation,
                                    maxLength = field.maxLength,
                                    showLimitCounter = true
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
                                FieldValidationText(field.validation)
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
                                FieldValidationText(field.validation)
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
                                FieldValidationText(field.validation)
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
                            val dateRangeMessage = when {
                                field.dateMin != null && field.dateMax != null ->
                                    stringResource(Res.string.question_input_date_out_of_range, formatDateForDisplay(field.dateMin), formatDateForDisplay(field.dateMax))
                                field.dateMin != null ->
                                    stringResource(Res.string.question_input_date_too_early, formatDateForDisplay(field.dateMin))
                                field.dateMax != null ->
                                    stringResource(Res.string.question_input_date_too_late, formatDateForDisplay(field.dateMax))
                                else -> null
                            }
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
                                FieldValidationText(field.validation, dateRangeMessage)
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
                                FieldValidationText(field.validation)
                            }
                        }

                        QuestionType.W -> {
                            val dateTimeRangeMessage = when {
                                field.dateMin != null && field.dateMax != null ->
                                    stringResource(Res.string.question_input_date_out_of_range, formatDateTimeForDisplay(field.dateMin), formatDateTimeForDisplay(field.dateMax))
                                field.dateMin != null ->
                                    stringResource(Res.string.question_input_date_too_early, formatDateTimeForDisplay(field.dateMin))
                                field.dateMax != null ->
                                    stringResource(Res.string.question_input_date_too_late, formatDateTimeForDisplay(field.dateMax))
                                else -> null
                            }
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
                                FieldValidationText(field.validation, dateTimeRangeMessage)
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
                                FieldValidationText(field.validation)
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
                ListDivider(index, form.lastIndex)
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
                val firstInvalidIndex = viewModel.validateForConfirm()
                if (firstInvalidIndex == null) {
                    onConfirm(viewModel.getCurrentAnswers(data))
                } else {
                    coroutineScope.launch {
                        state.animateScrollToItem(firstInvalidIndex + 1)
                    }
                }
            }
        )
    }

    if (modalQuestion != null && modalQuestion?.fieldType == QuestionType.F) {
        QuestionPhoto(onDismiss = { viewModel.dismissModal(it) })
    }
}

private fun String.formatNumber(): String =
    try {
        java.math.BigDecimal(this).stripTrailingZeros().toPlainString()
    } catch (_: NumberFormatException) {
        this
    }