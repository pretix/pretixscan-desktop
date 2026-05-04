package eu.pretix.scan.tickets.presentation
import com.vanniktech.locale.Country


fun Country.readableName(): String {
    if (name.length <= 4 && name.all { it.isUpperCase() || it == '_' }) {
        return name
    }
    return name.lowercase().replace(Regex("""(?:^|_+)(\p{L})""")) { (if (it.value.startsWith("_")) " " else "") + it.groupValues[1].uppercase() }
}
