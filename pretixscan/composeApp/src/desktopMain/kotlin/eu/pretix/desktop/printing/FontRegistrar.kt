package eu.pretix.desktop.printing

import eu.pretix.libpretixprint.templating.FontRegistry
import eu.pretix.libpretixprint.templating.FontSpecification.Style
import org.jetbrains.compose.resources.ExperimentalResourceApi
import pretixscan.composeapp.generated.resources.Res
import java.io.File

/*
* Helper class to deal with registering fonts
* */
class FontRegistrar {
    /**
     * To register fonts with libpretixprint, we need to pass the path to the actual font resource.
     * At this time, paths returned by [Res.getUri] do not appear to be valid when used to create a raw [File].
     * As a work-around, this method will read the contents of all font files and copy them to the user's cache directory.
     *
     * */
    suspend fun exportAndRegisterAllFonts(dataDir: String) {
        val fontsDir = File(dataDir, "files/fonts")
        if (!fontsDir.exists()) {
            fontsDir.mkdirs()
        }
        registerFontFamilies(dataDir, "Open Sans", "files/fonts/OpenSans-%s.ttf")
        registerFontFamilies(
            dataDir,
            "Almarai",
            "files/fonts/almarai-v5-arabic-%s.ttf",
            "regular",
            "800",
            "800",
            "regular"
        )
        registerFontFamilies(
            dataDir,
            "Baloo Bhaijaan",
            "files/fonts/baloo-bhaijaan-v6-latin-ext_vietnamese_latin_arabic-%s.ttf",
            "regular",
            "regular",
            "regular",
            "regular"
        )
        registerFontFamilies(dataDir, "Noto Sans", "files/fonts/NotoSans-%s-webfont.ttf")
        registerFontFamilies(
            dataDir,
            "Noto Sans Japanese",
            "files/fonts/noto-sans-jp-v52-cyrillic_japanese_latin_latin-ext_vietnamese-%s.ttf",
            "regular",
            "700",
            "700",
            "regular"
        )
        registerFontFamilies(
            dataDir,
            "Noto Sans Traditional Chinese",
            "files/fonts/noto-sans-tc-v35-chinese-traditional_cyrillic_latin_latin-ext_vietnamese-%s.ttf",
            "regular",
            "700",
            "700",
            "regular"
        )
        registerFontFamilies(
            dataDir,
            "Noto Sans Simplified Chinese",
            "files/fonts/noto-sans-sc-v36-chinese-simplified_cyrillic_latin_latin-ext_vietnamese-%s.ttf",
            "regular",
            "700",
            "700",
            "regular"
        )

        registerFontFamilies(dataDir, "Roboto", "files/fonts/Roboto-%s.ttf")
        registerFontFamilies(dataDir, "Droid Serif", "files/fonts/DroidSerif-%s-webfont.ttf")
        registerFontFamilies(dataDir, "Fira Sans", "files/fonts/firasans-%s-webfont.ttf")
        registerFontFamilies(dataDir, "Lato", "files/fonts/Lato-%s.ttf")
        registerFontFamilies(dataDir, "Vollkorn", "files/fonts/Vollkorn-%s.ttf")
        registerFontFamilies(dataDir, "Montserrat", "files/fonts/montserrat-%s-webfont.ttf")
        registerFontFamilies(dataDir, "Oswald", "files/fonts/oswald-%s-webfont.ttf")
        registerFontFamilies(dataDir, "Roboto Condensed", "files/fonts/RobotoCondensed-%s-webfont.ttf")
        registerFontFamilies(
            dataDir,
            "Tajawal",
            "files/fonts/tajawal-v3-latin_arabic-%s.ttf",
            "regular",
            "700",
            "700",
            "regular"
        )
        registerFontFamilies(dataDir, "Titillium", "files/fonts/titillium-%s-webfont.ttf")
        registerFontFamilies(
            dataDir,
            "Titillium Upright",
            "files/fonts/titillium-%s-webfont.ttf",
            "RegularUpright",
            "BoldUpright",
            "BoldUpright",
            "RegularUpright"
        )
        registerFontFamilies(
            dataDir,
            "Titillium Semibold Upright",
            "files/fonts/titillium-%s-webfont.ttf",
            "SemiboldUpright",
            "BoldUpright",
            "BoldUpright",
            "SemiboldUpright"
        )
        registerFontFamilies(dataDir, "DejaVu Sans", "files/fonts/DejaVuSans-%s-webfont.ttf")
        registerFontFamilies(dataDir, "Poppins", "files/fonts/Poppins-%s-webfont.ttf")
        registerFontFamilies(dataDir, "Space Mono", "files/fonts/Space-Mono-%s.ttf")
        registerFontFamilies(
            dataDir,
            "Ubuntu",
            "files/fonts/ubuntu-v15-latin-ext_latin-%s.ttf",
            "regular",
            "700",
            "700italic",
            "italic"
        )
    }


    @OptIn(ExperimentalResourceApi::class)
    private suspend fun exportFont(dataDir: String, path: String): String {
        val bytes = Res.readBytes(path)
        val file = File(dataDir, path)
        if (file.exists()) {
            // already done
            return file.absolutePath
        }
        file.writeBytes(bytes)
        return file.absolutePath
    }

    private suspend fun registerFontFamilies(
        dataDir: String,
        name: String,
        pattern: String,
        regularName: String = "Regular",
        boldName: String = "Bold",
        boldItalicName: String = "BoldItalic",
        italicName: String = "Italic"
    ) {
        val fontRegistry = FontRegistry.getInstance()

        fontRegistry.add(name, Style.REGULAR, exportFont(dataDir, String.format(pattern, regularName)))
        fontRegistry.add(name, Style.BOLDITALIC, exportFont(dataDir, String.format(pattern, boldItalicName)))
        fontRegistry.add(name, Style.BOLD, exportFont(dataDir, String.format(pattern, boldName)))
        fontRegistry.add(name, Style.ITALIC, exportFont(dataDir, String.format(pattern, italicName)))
    }
}