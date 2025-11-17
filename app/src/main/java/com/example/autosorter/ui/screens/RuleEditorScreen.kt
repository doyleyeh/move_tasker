package com.example.autosorter.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.autosorter.data.entity.FileTypeFilter
import com.example.autosorter.viewmodel.RulesViewModel
import java.util.Locale
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditorScreen(
    ruleId: Long?,
    viewModel: RulesViewModel,
    onFinished: () -> Unit
) {
    val ruleFlow = remember(ruleId) {
        ruleId?.let { viewModel.observeRule(it) } ?: flowOf(null)
    }
    val existingRule by ruleFlow.collectAsState(initial = null)

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var name by rememberSaveable { mutableStateOf(existingRule?.name ?: "") }
    var source by rememberSaveable { mutableStateOf(existingRule?.sourcePath ?: "") }
    var destination by rememberSaveable { mutableStateOf(existingRule?.destinationPath ?: "") }
    var extensions by rememberSaveable { mutableStateOf(existingRule?.extensionsFilter ?: "") }
    var enabled by rememberSaveable { mutableStateOf(existingRule?.enabled ?: true) }
    var fileType by rememberSaveable { mutableStateOf(existingRule?.fileTypeFilter ?: FileTypeFilter.IMAGE) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }

    val sourcePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            persistUriPermission(context, it)
            source = formatDirectoryDisplayName(it)
        }
    }
    val destinationPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            persistUriPermission(context, it)
            destination = formatDirectoryDisplayName(it)
        }
    }

    val openSourceBrowser = { sourcePicker.launch(null) }
    val openDestinationBrowser = { destinationPicker.launch(null) }

    LaunchedEffect(existingRule) {
        existingRule?.let { rule ->
            name = rule.name
            source = rule.sourcePath
            destination = rule.destinationPath
            extensions = rule.extensionsFilter.orEmpty()
            enabled = rule.enabled
            fileType = rule.fileTypeFilter
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (ruleId == null) "Add Rule" else "Edit Rule") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Section(title = "Rule details") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Rule name") }
                )
            }
            Section(title = "Folder locations") {
                DirectoryField(
                    label = "Source folder path",
                    value = source,
                    onBrowse = openSourceBrowser,
                    supportingText = "Tap to pick the folder the files start in."
                )
                Spacer(modifier = Modifier.height(2.dp))
                DirectoryField(
                    label = "Destination folder path",
                    value = destination,
                    onBrowse = openDestinationBrowser,
                    supportingText = "Where matching files should be moved."
                )
            }
            Section(title = "File filters") {
                val density = LocalDensity.current
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = fileType.name.lowercase().replaceFirstChar { it.titlecase(Locale.ROOT) },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                textFieldSize = coordinates.size
                            },
                        label = { Text("File type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = if (textFieldSize.width > 0) {
                            Modifier.width(with(density) { textFieldSize.width.toDp() })
                        } else {
                            Modifier
                        }
                    ) {
                        FileTypeFilter.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(option.name.lowercase().replaceFirstChar { it.titlecase(Locale.ROOT) })
                                },
                                onClick = {
                                    fileType = option
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = extensions,
                    onValueChange = { extensions = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Extensions (comma separated)") },
                    supportingText = {
                        val examples = when (fileType) {
                            FileTypeFilter.IMAGE -> "e.g. jpg, png, webp"
                            FileTypeFilter.VIDEO -> "e.g. mp4, mov, mkv"
                            else -> "Leave empty to include all"
                        }
                        Text(examples)
                    }
                )
                if (fileType == FileTypeFilter.IMAGE || fileType == FileTypeFilter.VIDEO) {
                    // Spacer(modifier = Modifier.height(2.dp))
                    val normalizedExtensions = extensions
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    val extensionSummary = if (normalizedExtensions.isEmpty()) {
                        "All ${fileType.name.lowercase(Locale.ROOT)} formats"
                    } else {
                        normalizedExtensions.joinToString(", ") { it.uppercase(Locale.ROOT) }
                    }
                    Text(
                        text = "Selected: $extensionSummary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                // verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status and Automation",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                Text(
                    text = "Keep on to watch the folder in the background.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onFinished,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        viewModel.saveRule(
                            id = existingRule?.id ?: 0L,
                            name = name.trim(),
                            source = source.trim(),
                            destination = destination.trim(),
                            typeFilter = fileType,
                            extensions = extensions.trim(),
                            enabled = enabled
                        )
                        onFinished()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank() && source.isNotBlank() && destination.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirectoryField(
    label: String,
    value: String,
    onBrowse: () -> Unit,
    supportingText: String
) {
    // We don't actually need a real expanded state, but the Box needs one.
    var dummyExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = dummyExpanded,
        onExpandedChange = {
            // Any tap on the whole bar comes here
            onBrowse()
        }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},          // read-only field
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text(label) },
            supportingText = { Text(supportingText) },
            trailingIcon = {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = "Browse $label"
                )
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        // No DropdownMenu here – we just use the box for its clickable styling.
    }
}

private fun formatDirectoryDisplayName(uri: Uri): String {
    val docId = DocumentsContract.getTreeDocumentId(uri)
    val parts = docId.split(":")
    val root = parts.getOrNull(0).orEmpty()
    val relativePath = parts.getOrNull(1).orEmpty()
    val basePath = when (root.lowercase(Locale.ROOT)) {
        "primary" -> "/storage/emulated/0"
        else -> root
    }
    return if (relativePath.isNotEmpty()) {
        "$basePath/${relativePath.replace(":", "/")}"
    } else {
        basePath
    }
}

private fun persistUriPermission(context: Context, uri: Uri) {
    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    try {
        context.contentResolver.takePersistableUriPermission(uri, flags)
    } catch (_: SecurityException) {
        // ignore if permission can't be persisted
    }
}
