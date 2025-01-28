package settings.presentation


import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.ui.CustomColor
import app.ui.FieldSpinner
import app.ui.asColor
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.*


@Composable
fun SettingsScreen(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    val viewModel = koinViewModel<SettingsViewModel>()
    val form by viewModel.form.collectAsState()
    val state = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    Column {
        Toolbar(onGoBack = {
            navHostController.popBackStack()
        })


        Box {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                state = state
            ) {
                item {
                    Text(
                        stringResource(Res.string.action_label_settings),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
//                item {
//                    Section(stringResource(Res.string.settings_label_verification)) {
//                        Setting {
//                            SettingCheckbox(
//                                label = stringResource(Res.string.settings_label_scan_offline),
//                                description = stringResource(Res.string.settings_summary_scan_offline),
//                                checked = true,
//                                onCheckedChange = {}
//                            )
//                        }
//
//                        Setting {
//                            SettingCheckbox(
//                                label = stringResource(Res.string.settings_label_scan_offline),
//                                description = stringResource(Res.string.settings_summary_scan_offline),
//                                checked = true,
//                                onCheckedChange = {}
//                            )
//                        }
//                    }
//                }

                item {
                    Section(stringResource(Res.string.settings_label_badges)) {
                        Setting {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    stringResource(Res.string.settings_printers_badge)
                                )
                                FieldSpinner(
                                    selectedValue = form.badgePrinter?.value,
                                    availableOptions = form.printers,
                                    onSelect = {
                                        coroutineScope.launch {
                                            viewModel.setBadgePrinter(it)
                                        }
                                    },
                                )
                            }
                        }

                        Setting {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    stringResource(Res.string.settings_label_badge_layout)
                                )
                                FieldSpinner(
                                    selectedValue = form.badgeLayout?.value,
                                    availableOptions = form.layouts,
                                    onSelect = {
                                        coroutineScope.launch {
                                            viewModel.setBadgePrinterLayout(it)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                item {
                    Section(stringResource(Res.string.settings_label_about)) {
                        Setting {
                            SettingLabel(
                                label = stringResource(Res.string.settings_label_version),
                                description = form.version
                            )
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
    }
}

@Composable
fun Section(heading: String, content: @Composable () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            heading,
            modifier = Modifier.padding(vertical = 16.dp),
            style = MaterialTheme.typography.titleSmall, color = CustomColor.BrandGreen.asColor(),
            fontWeight = FontWeight.Medium
        )
        content()
        HorizontalDivider()
    }
}

@Composable
fun Setting(content: @Composable () -> Unit) {
    Row(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
        content()
    }
}