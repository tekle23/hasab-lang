package hasab.runtime.io

import java.io.Console
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

/**
 * Utility object providing HASAB standard I/O operations.
 *
 * This object wraps standard Java I/O facilities and provides
 * convenience methods for reading and writing data from/to the console
 * and streams.
 */
public object HsIO {

    /**
     * Prints the given message to standard output without a trailing newline.
     *
     * @param message The message to print.
     */
    public fun print(message: Any?) {
        System.out.print(message)
    }

    /**
     * Prints the given message to standard output, followed by a newline.
     *
     * @param message The message to print.
     */
    public fun println(message: Any?) {
        System.out.println(message)
    }

    /**
     * Prints a newline to standard output.
     */
    public fun println() {
        System.out.println()
    }

    /**
     * Reads a single line of input from standard input.
     *
     * @return The line read, or `null` if the end of the input stream has been reached.
     */
    public fun readLine(): String? = try {
        System.`in`.bufferedReader().readLine()
    } catch (_: Exception) {
        null
    }

    /**
     * Reads a password from standard input without echoing.
     *
     * @return The password as a [CharArray], or `null` if input is not available.
     */
    public fun readPassword(): CharArray? {
        val console: Console? = System.console()
        return console?.readPassword()
    }

    /**
     * Reads a line of input from standard input, optionally displaying a prompt.
     *
     * @param prompt The prompt to display before reading input.
     * @return The input line, or an empty string if no input was provided.
     */
    public fun readInput(prompt: String = ""): String {
        if (prompt.isNotEmpty()) {
            print(prompt)
        }
        return readLine() ?: ""
    }

    /**
     * Copies all bytes from an input stream to an output stream.
     *
     * @param input The source input stream.
     * @param output The destination output stream.
     * @return The total number of bytes transferred.
     */
    public fun copyStream(input: InputStream, output: OutputStream): Long {
        val buffer = ByteArray(8192)
        var totalBytes = 0L
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
            totalBytes += bytesRead
        }
        output.flush()
        return totalBytes
    }

    /**
     * Reads all bytes from an input stream into a byte array.
     *
     * @param inputStream The input stream to read from.
     * @return The byte array containing all data from the stream.
     */
    public fun readAllBytes(inputStream: InputStream): ByteArray =
        inputStream.readBytes()

    /**
     * Reads all text from an input stream using the specified charset.
     *
     * @param inputStream The input stream to read from.
     * @param charset The character set to use for decoding.
     * @return The decoded string content.
     */
    public fun readAllText(
        inputStream: InputStream,
        charset: String = "UTF-8"
    ): String =
        inputStream.bufferedReader(Charset.forName(charset)).use { it.readText() }

    /**
     * Writes a byte array to an output stream.
     *
     * @param outputStream The destination output stream.
     * @param bytes The bytes to write.
     */
    public fun writeAllBytes(outputStream: OutputStream, bytes: ByteArray) {
        outputStream.write(bytes)
        outputStream.flush()
    }

    /**
     * Writes text to an output stream using the specified charset.
     *
     * @param outputStream The destination output stream.
     * @param text The text to write.
     * @param charset The character set to use for encoding.
     */
    public fun writeAllText(
        outputStream: OutputStream,
        text: String,
        charset: String = "UTF-8"
    ) {
        outputStream.bufferedWriter(Charset.forName(charset)).use { it.write(text) }
        outputStream.flush()
    }

    /**
     * Opens a classpath resource as an [InputStream].
     *
     * @param resourcePath The path to the resource on the classpath.
     * @return The [InputStream] for the resource, or `null` if the resource was not found.
     */
    public fun openResource(resourcePath: String): InputStream? =
        object {}.javaClass.getResourceAsStream(resourcePath)

    /**
     * Reads a classpath resource as a UTF-8 string.
     *
     * @param resourcePath The path to the resource on the classpath.
     * @return The resource content as a string, or `null` if the resource was not found.
     */
    public fun readResource(resourcePath: String): String? =
        openResource(resourcePath)?.use { readAllText(it, "UTF-8") }

    /**
     * Reads a classpath resource as a byte array.
     *
     * @param resourcePath The path to the resource on the classpath.
     * @return The resource content as bytes, or `null` if the resource was not found.
     */
    public fun readResourceBytes(resourcePath: String): ByteArray? =
        openResource(resourcePath)?.use { readAllBytes(it) }
}
