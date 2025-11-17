package com.example.autosorter.util

import java.io.File
import java.io.IOException

object FileMover {
    fun moveFile(source: File, destinationDir: File): File {
        if (!destinationDir.exists()) {
            destinationDir.mkdirs()
        }
        val destinationFile = File(destinationDir, source.name)
        val success = source.renameTo(destinationFile)
        if (!success) {
            // Fallback to copy + delete for cross-filesystem moves
            source.copyTo(destinationFile, overwrite = true)
            if (!source.delete()) {
                throw IOException("Failed to delete original file: ${source.absolutePath}")
            }
        }
        return destinationFile
    }
}
