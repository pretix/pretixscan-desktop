package eu.pretix.pretixscan.desktop

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


@Throws(IOException::class)
fun readFromInputStream(inputStream: InputStream): String {
    val resultStringBuilder = StringBuilder()
    BufferedReader(InputStreamReader(inputStream)).use { br ->
        var line: String?
        while (true) {
            line = br.readLine()
            if (line == null) {
                break;
            }
            resultStringBuilder.append(line).append("\n")
        }
    }
    return resultStringBuilder.toString()
}