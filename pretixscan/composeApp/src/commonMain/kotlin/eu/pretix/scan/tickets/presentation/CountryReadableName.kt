package eu.pretix.scan.tickets.presentation
import com.vanniktech.locale.Country


fun Country.readableName(): String {
    return name.lowercase().replace(Regex("""(?:^|_+)(\p{L})""")) { (if (it.value.startsWith("_")) " " else "") + it.groupValues[1].uppercase() } // LIKE_THIS -> "Like This"
}
