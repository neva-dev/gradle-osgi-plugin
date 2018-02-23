package com.neva.gradle.osgi.internal

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.gradle.util.GFileUtils
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FileOperations {

    const val PKG = "com.neva.gradle.osgi"

    fun readResource(path: String): InputStream? {
        return javaClass.getResourceAsStream("/${PKG.replace(".", "/")}/$path")
    }

    fun getResources(path: String): List<String> {
        return Reflections("$PKG.$path".replace("/", "."), ResourcesScanner()).getResources { true; }.toList()
    }

    fun eachResource(resourceRoot: String, targetDir: File, callback: (String, File) -> Unit) {
        for (resourcePath in getResources(resourceRoot)) {
            val outputFile = File(targetDir, resourcePath.substringAfterLast("$resourceRoot/"))

            callback(resourcePath, outputFile)
        }
    }

    fun copyResources(resourceRoot: String, targetDir: File, skipExisting: Boolean = false) {
        eachResource(resourceRoot, targetDir, { resourcePath, outputFile ->
            if (!skipExisting || !outputFile.exists()) {
                copyResource(resourcePath, outputFile)
            }
        })
    }

    fun copyResource(resourcePath: String, outputFile: File) {
        GFileUtils.mkdirs(outputFile.parentFile)

        val input = javaClass.getResourceAsStream("/" + resourcePath)
        val output = FileOutputStream(outputFile)

        try {
            IOUtils.copy(input, output)
        } finally {
            IOUtils.closeQuietly(input)
            IOUtils.closeQuietly(output)
        }
    }

    fun amendFiles(dir: File, wildcardFilters: List<String>, amender: (File, String) -> String) {
        val files = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
        files?.filter { Patterns.wildcard(it, wildcardFilters) }?.forEach { file ->
            val source = amender(file, file.inputStream().bufferedReader().use { it.readText() })
            file.printWriter().use { it.print(source) }
        }
    }

    fun amendFile(file: File, amender: (String) -> String) {
        val source = amender(file.inputStream().bufferedReader().use { it.readText() })
        file.printWriter().use { it.print(source) }
    }


    fun isDirEmpty(dir: File): Boolean {
        return dir.exists() && isDirEmpty(Paths.get(dir.absolutePath))
    }

    fun isDirEmpty(dir: Path): Boolean {
        Files.newDirectoryStream(dir).use({ dirStream -> return !dirStream.iterator().hasNext() })
    }

}