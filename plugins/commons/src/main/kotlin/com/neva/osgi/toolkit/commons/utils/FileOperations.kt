package com.neva.osgi.toolkit.commons.utils

import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileOperations {

    fun readResource(path: String): InputStream? {
        return javaClass.getResourceAsStream("/$path")
    }

    fun getResources(path: String): List<String> {
        val pkg = path.replace("/", ".")
        val reflections = Reflections(pkg, ResourcesScanner())

        return reflections.getResources { true; }.toList()
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
        outputFile.parentFile.mkdirs()

        val input = javaClass.getResourceAsStream("/" + resourcePath)
        val output = FileOutputStream(outputFile)

        input.copyTo(output)
    }

}
