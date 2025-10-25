package eu.pretix.scan.tickets.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.ImeAction
import org.jetbrains.compose.resources.stringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.text_action_clear
import java.util.logging.Logger

private val log = Logger.getLogger("SearchTextField")

@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    hint: String = "",
    onSearch: (String) -> Unit = {},
    onDirectScan: (String) -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    // Barcode pattern from old implementation: alphanumeric plus =+/ with at least 5 chars
    val barcodePattern = remember { Regex("[a-zA-Z0-9=+/]{5,}") }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    var text by remember {
        mutableStateOf(value)
    }

    Box {
        TextField(
            value = text,
            placeholder = {
                Text(hint)
            },
            onValueChange = {
                text = it
                onSearch(it)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                // Check if this looks like a barcode scan
                if (text.matches(barcodePattern)) {
                    log.info("AutoScan: Barcode pattern detected, triggering direct scan")
                    // Direct check-in for barcode
                    onDirectScan(text)
                    text = ""
                    // Clear search results by notifying ViewModel
                    onSearch("")
                } else {
                    log.info("AutoScan: Regular search (not barcode pattern)")
                    // Normal search
                    onSearch(text)
                }
            }),
            maxLines = 1,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            trailingIcon = {
                when {
                    text.isNotEmpty() -> IconButton(onClick = {
                        text = ""
                        onSearch("")
                    }, modifier = Modifier.pointerHoverIcon(PointerIcon.Default)) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = stringResource(Res.string.text_action_clear)
                        )
                    }
                }
            }
        )
    }
}