package eu.pretix.desktop.nfc

import eu.pretix.libpretixnfc.communication.ChipReadError
import org.jetbrains.compose.resources.StringResource
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.error_unknown_exception
import pretixscan.composeapp.generated.resources.nfc_empty_chip
import pretixscan.composeapp.generated.resources.nfc_foreign_chip
import pretixscan.composeapp.generated.resources.nfc_read_error
import pretixscan.composeapp.generated.resources.nfc_unknown_chip_type

fun ChipReadError.stringResource(): StringResource = when (this) {
    ChipReadError.IO_ERROR -> Res.string.nfc_read_error
    ChipReadError.UNKNOWN_CHIP_TYPE -> Res.string.nfc_unknown_chip_type
    ChipReadError.FOREIGN_CHIP -> Res.string.nfc_foreign_chip
    ChipReadError.EMPTY_CHIP -> Res.string.nfc_empty_chip
    ChipReadError.UNKNOWN_ERROR -> Res.string.error_unknown_exception
}
