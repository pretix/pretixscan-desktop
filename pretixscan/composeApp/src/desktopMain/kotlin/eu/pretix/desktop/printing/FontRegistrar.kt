package eu.pretix.desktop.printing

import eu.pretix.libpretixprint.templating.FontRegistry
import eu.pretix.libpretixprint.templating.FontSpecification.Style
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.json.JSONObject
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
            "Baloo Bhaijaan",
            "files/fonts/baloo-bhaijaan-v6-latin-ext_vietnamese_latin_arabic-%s.ttf",
            "regular",
            "regular",
            "regular",
            "regular"
        )
        registerFontFamilies(dataDir, "Droid Serif", "files/fonts/DroidSerif-%s-webfont.ttf")
        registerFontFamilies(
            dataDir,
            "Titillium Upright",
            "files/fonts/titillium-%s-webfont.ttf",
            "regularupright",
            "boldupright",
            "boldupright",
            "regularupright"
        )
        registerFontFamilies(
            dataDir,
            "Titillium Semibold Upright",
            "files/fonts/titillium-%s-webfont.ttf",
            "semiboldupright",
            "boldupright",
            "boldupright",
            "semiboldupright"
        )
        registerFontFamilies(dataDir, "DejaVu Sans", "files/fonts/DejaVuSans-%s-webfont.ttf")
        val cat_json = Res.readBytes("files/fonts/catalog.json").decodeToString()
        val cat = JSONObject(cat_json)
        for (family in cat.keys()) {
            val familyConfig = cat.getJSONObject(family)
            val regularName = familyConfig.getJSONObject("regular").getString("truetype")
            val boldName = if (familyConfig.has("bold")) familyConfig.getJSONObject("bold").getString("truetype") else regularName
            val italicName = if (familyConfig.has("italic")) familyConfig.getJSONObject("italic").getString("truetype") else regularName
            val boldItalicName = if (familyConfig.has("bolditalic")) familyConfig.getJSONObject("bolditalic").getString("truetype") else regularName
            registerFontFamilies(dataDir, family, "files/fonts/%s", regularName, boldName, boldItalicName, italicName)
        }
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