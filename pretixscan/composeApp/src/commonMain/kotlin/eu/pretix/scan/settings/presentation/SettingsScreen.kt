package eu.pretix.scan.settings.presentation


//import androidx.compose.material3.Button
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.composeunstyled.Icon
import eu.pretix.desktop.app.navigation.Route
import eu.pretix.desktop.app.ui.*
import eu.pretix.desktop.cache.getLogDirectory
import eu.pretix.desktop.cache.getUserDataFolder
import eu.pretix.desktop.cache.openPathInFileBrowser
import eu.pretix.desktop.webcam.data.VideoSource
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.*


@Composable
fun SettingsScreen(
    navHostController: NavHostController,
) {
    val coroutineScope = rememberCoroutineScope()

    val viewModel = koinViewModel<SettingsViewModel>()
    val form by viewModel.form.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val state = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    Column {
        Toolbar(onGoBack = {
            navHostController.popBackStack()
        })

        ScreenContentRoot {
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
                    item {
                        Section(stringResource(Res.string.settings_label_verification)) {
                            Setting {
                                Column(
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    CheckboxWithLabel(
                                        label = stringResource(Res.string.settings_label_scan_offline),
                                        description = null,
                                        checked = form.offlineMode,
                                        onCheckedChange = {
                                            coroutineScope.launch {
                                                viewModel.setOfflineMode(it)
                                            }
                                        }
                                    )
                                }
                            }
                            Setting {
                                Column(
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    CheckboxWithLabel(
                                        label = stringResource(Res.string.pref_unpaid_ask),
                                        description = stringResource(Res.string.pref_unpaid_ask_summary),
                                        checked = form.unpaidAsk,
                                        onCheckedChange = {
                                            coroutineScope.launch {
                                                viewModel.setUnpaidAsk(it)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Section(stringResource(Res.string.settings_label_sync)) {
                            Setting {
                                Column(
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    CheckboxWithLabel(
                                        label = stringResource(Res.string.settings_label_auto_sync),
                                        description = null,
                                        checked = form.syncAuto,
                                        onCheckedChange = {
                                            coroutineScope.launch {
                                                viewModel.setSyncAuto(it)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Section(stringResource(Res.string.settings_label_ui)) {
                            Setting {
                                Column(
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    CheckboxWithLabel(
                                        label = stringResource(Res.string.settings_label_sounds),
                                        description = null,
                                        checked = form.playSounds,
                                        onCheckedChange = {
                                            coroutineScope.launch {
                                                viewModel.setPlaySounds(it)
                                            }
                                        }
                                    )
                                }
                            }

                            Setting {
                                Column(
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    CheckboxWithLabel(
                                        label = stringResource(Res.string.settings_label_ui_reduce_motion),
                                        description = stringResource(Res.string.settings_label_ui_reduce_motion_summary),
                                        checked = form.uiReduceMotion,
                                        onCheckedChange = {
                                            coroutineScope.launch {
                                                viewModel.setUiReduceMotion(it)
                                            }
                                        }
                                    )
                                }
                            }

                            Setting {
                                Column(
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    CheckboxWithLabel(
                                        label = stringResource(Res.string.settings_label_hide_names),
                                        description = stringResource(Res.string.settings_label_hide_names_summary),
                                        checked = form.uiHideNames,
                                        onCheckedChange = {
                                            coroutineScope.launch {
                                                viewModel.setUiHideNames(it)
                                            }
                                        }
                                    )
                                }
                            }
                        }
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
                                    CheckboxWithLabel(
                                        label = stringResource(Res.string.settings_label_print_badges),
                                        description = null,
                                        checked = form.printBadges,
                                        onCheckedChange = {
                                            coroutineScope.launch {
                                                viewModel.setPrintBadges(it)
                                            }
                                        }
                                    )
                                }
                            }

                            if (form.printBadges) {
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
                    }

                    item {
                        Section(stringResource(Res.string.settings_section_camera)) {
                            Setting {
                                Column(
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        stringResource(Res.string.settings_preferred_camera_label)
                                    )
                                    FieldSpinner(
                                        selectedValue = form.preferredCamera,
                                        availableOptions = listOf(SelectableValue(VideoSource.NO_CAMERA_NAME, stringResource(Res.string.settings_camera_auto))) + form.cameras.map { SelectableValue(it, it) },
                                        onSelect = {
                                            coroutineScope.launch {
                                                viewModel.setPreferredCamera(it?.value)
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

                    item {
                        Section(stringResource(Res.string.settings_label_diagnostics)) {
                            Setting {
                                PrimaryButton(
                                    onClick = {
                                        openPathInFileBrowser(getUserDataFolder())
                                    },
                                    label = stringResource((Res.string.open_data_folder_action)),
                                    icon = { Icon(Icons.Default.Folder, contentDescription = null) }
                                )
                                Spacer(Modifier.width(16.dp))
                                PrimaryButton(
                                    onClick = {
                                        openPathInFileBrowser(getLogDirectory())
                                    },
                                    label = stringResource((Res.string.open_logs_folder_action)),
                                    icon = { Icon(Icons.Default.Folder, contentDescription = null) }
                                )
                            }
                        }
                    }

                    item {
                        Section(stringResource(Res.string.full_delete)) {
                            Setting {
                                PrimaryButton(
                                    onClick = {
                                        // TODO: confirm reset
                                        viewModel.logout()
                                        navHostController.popBackStack()
                                        navHostController.navigate(Route.Welcome.route)
                                    },
                                    label = stringResource(Res.string.full_delete_action)
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

    when (uiState) {
        is SettingsUiState.Start -> {}
        is SettingsUiState.ErrorNoAvailablePrinters -> {
            ErrorDialog(
                title = stringResource(Res.string.badge_printing_not_available),
                message = stringResource(Res.string.badge_printing_not_possible_no_available_printer),
                onDismiss = {
                    viewModel.dismissError()
                }
            )
        }

        is SettingsUiState.ErrorSelectedPrinterNotAvailable -> {
            ErrorDialog(
                title = stringResource(Res.string.badge_printing_not_available),
                message = stringResource(Res.string.badge_printing_selected_printer_not_available),
                onDismiss = {
                    viewModel.dismissError()
                }
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
        ListDivider()
    }
}

@Composable
fun Setting(content: @Composable () -> Unit) {
    Row(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
        content()
    }
}