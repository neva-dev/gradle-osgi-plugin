package com.neva.osgi.toolkit.gradle.internal

import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ResourceOperations {

    fun read(path: String): InputStream? {
        return javaClass.getResourceAsStream("/$path")
    }

    fun readDir(path: String): List<String> {
        val pkg = path.replace("/", ".")
        val reflections = Reflections(pkg, ResourcesScanner())

        return reflections.getResources { true; }.toList()
    }

    fun listDir(resourceRoot: String, targetDir: File, callback: (String, File) -> Unit) {
        for (resourcePath in readDir(resourceRoot)) {
            val outputFile = File(targetDir, resourcePath.substringAfterLast("$resourceRoot/"))

            callback(resourcePath, outputFile)
        }
    }

    fun copyDir(resourceRoot: String, targetDir: File, skipExisting: Boolean = false) {
        listDir(resourceRoot, targetDir, { resourcePath, outputFile ->
            if (!skipExisting || !outputFile.exists()) {
                copy(resourcePath, outputFile)
            }
        })
    }

    fun copy(resourcePath: String, outputFile: File) {
        outputFile.parentFile.mkdirs()

        val input = javaClass.getResourceAsStream("/" + resourcePath)
        val output = FileOutputStream(outputFile)

        input.copyTo(output)
    }

}
