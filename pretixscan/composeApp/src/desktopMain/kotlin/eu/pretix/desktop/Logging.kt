package eu.pretix.desktop

import java.io.OutputStream
import java.io.PrintStream
import java.util.logging.Level
import java.util.logging.Logger

internal fun installSystemStreamLoggers() {
    System.setOut(loggingPrintStream("System.out", Level.INFO))
    System.setErr(loggingPrintStream("System.err", Level.WARNING))
}

internal fun installUncaughtExceptionLogger() {
    val logger = Logger.getLogger("UncaughtException")
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        logger.log(Level.SEVERE, "Uncaught exception in thread ${thread.name}", throwable)
    }
}

private fun loggingPrintStream(name: String, level: Level): PrintStream {
    val logger = Logger.getLogger(name)
    val buffer = ThreadLocal.withInitial { StringBuilder() }

    val stream = object : OutputStream() {
        override fun write(b: Int) {
            val ch = b.toChar()
            if (ch == '\n') {
                logger.log(level, buffer.get().toString())
                buffer.get().setLength(0)
            } else if (ch != '\r') {
                buffer.get().append(ch)
            }
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            var lineStart = off
            val end = off + len
            for (i in off until end) {
                val ch = b[i].toInt().toChar()
                if (ch == '\n') {
                    if (i > lineStart) {
                        val segment = String(b, lineStart, i - lineStart, Charsets.UTF_8)
                            .trimEnd('\r')
                        buffer.get().append(segment)
                    }
                    logger.log(level, buffer.get().toString())
                    buffer.get().setLength(0)
                    lineStart = i + 1
                }
            }
            if (lineStart < end) {
                buffer.get().append(String(b, lineStart, end - lineStart, Charsets.UTF_8))
            }
        }

        override fun flush() = Unit
    }

    return PrintStream(stream, true, Charsets.UTF_8)
}
