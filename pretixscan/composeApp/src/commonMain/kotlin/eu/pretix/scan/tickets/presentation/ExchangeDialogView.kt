package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.asColor
import eu.pretix.desktop.nfc.NfcState
import eu.pretix.libpretixsync.db.ReusableMediaType
import eu.pretix.scan.tickets.data.ExchangeSupport
import eu.pretix.scan.tickets.data.ResultState
import eu.pretix.scan.tickets.data.ResultStateData
import eu.pretix.scan.tickets.data.color
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.cancel
import pretixscan.composeapp.generated.resources.ic_error_white_24dp
import pretixscan.composeapp.generated.resources.nfc_no_reader
import pretixscan.composeapp.generated.resources.ok
import pretixscan.composeapp.generated.resources.reusable_media_exchange_needed
import pretixscan.composeapp.generated.resources.reusable_media_exchange_nfc_scan
import pretixscan.composeapp.generated.resources.reusable_media_exchange_no_nfc_support
import pretixscan.composeapp.generated.resources.reusable_media_exchange_not_implemented

@Composable
fun ExchangeDialogView(
    modifier: Modifier = Modifier,
    data: ResultStateData,
    onMediumScanned: (String, ReusableMediaType) -> Unit,
    onCancel: () -> Unit
) {
    val viewModel = koinViewModel<ExchangeDialogViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(data) {
        viewModel.start(data.requiredMediaType, data.requiredMediaPolicy)
        onDispose {
            viewModel.stop()
        }
    }

    LaunchedEffect(uiState.scannedMedium) {
        val scannedMedium = uiState.scannedMedium ?: return@LaunchedEffect
        onMediumScanned(scannedMedium.first, scannedMedium.second)
    }

    when (uiState.support) {
        ExchangeSupport.NOT_IMPLEMENTED -> ExchangeRefused(
            modifier = modifier,
            data = data,
            reason = stringResource(Res.string.reusable_media_exchange_not_implemented),
            onConfirm = onCancel
        )

        ExchangeSupport.NO_NFC -> ExchangeRefused(
            modifier = modifier,
            data = data,
            reason = stringResource(Res.string.reusable_media_exchange_no_nfc_support),
            onConfirm = onCancel
        )

        ExchangeSupport.SUPPORTED -> Column(
            modifier = modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(Res.string.reusable_media_exchange_needed),
                style = MaterialTheme.typography.titleLarge
            )

            if (!data.ticketAndVariationName.isNullOrBlank()) {
                Text(data.ticketAndVariationName, style = MaterialTheme.typography.bodyLarge)
            }

            TicketResultDetails(data)

            val warning = uiState.error?.let { stringResource(it) }
                ?: stringResource(Res.string.nfc_no_reader).takeIf { uiState.nfcState == NfcState.DISABLED }
            if (warning != null) {
                ExchangeWarning(warning)
            }

            Icon(
                Icons.Default.Nfc,
                contentDescription = stringResource(Res.string.reusable_media_exchange_nfc_scan),
                modifier = Modifier.size(48.dp)
            )

            Text(stringResource(Res.string.reusable_media_exchange_nfc_scan))

            Button(onClick = onCancel) {
                Text(stringResource(Res.string.cancel))
            }
        }
    }
}

@Composable
private fun ExchangeRefused(
    modifier: Modifier = Modifier,
    data: ResultStateData,
    reason: String,
    onConfirm: () -> Unit
) {
    val refusal = data.copy(
        resultState = ResultState.ERROR,
        resultText = stringResource(Res.string.reusable_media_exchange_needed),
        reasonExplanation = reason
    )

    Column(modifier = modifier.background(refusal.resultState.color())) {
        TicketResultHeader(icon = Res.drawable.ic_error_white_24dp, data = refusal)

        TicketResultDetails(refusal)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onConfirm) {
                Text(stringResource(Res.string.ok))
            }
        }
    }
}

@Composable
private fun ExchangeWarning(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CustomColor.BrandOrange.asColor())
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}
