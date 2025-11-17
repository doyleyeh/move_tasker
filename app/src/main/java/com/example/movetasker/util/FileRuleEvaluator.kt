package com.example.movetasker.util

import com.example.movetasker.data.entity.FileTypeFilter
import com.example.movetasker.data.entity.RuleEntity
import java.io.File
import java.util.Locale

object FileRuleEvaluator {
    private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "heic", "heif", "bmp")
    private val videoExtensions = setOf("mp4", "mov", "avi", "mkv", "webm")
    private val documentExtensions = setOf("pdf", "txt", "doc", "docx")

    fun matches(rule: RuleEntity, file: File): Boolean {
        if (!file.exists() || !file.isFile) return false
        val extension = file.extension.lowercase(Locale.US)
        val extensionFilters = rule.extensionsFilter?.split(',')
            ?.map { it.trim().lowercase(Locale.US) }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
        if (!extensionFilters.isNullOrEmpty()) {
            return extensionFilters.contains(extension)
        }
        return when (rule.fileTypeFilter) {
            FileTypeFilter.ALL -> true
            FileTypeFilter.IMAGE -> imageExtensions.contains(extension)
            FileTypeFilter.VIDEO -> videoExtensions.contains(extension)
            FileTypeFilter.OTHER -> !(imageExtensions + videoExtensions + documentExtensions).contains(extension)
        }
    }
}
