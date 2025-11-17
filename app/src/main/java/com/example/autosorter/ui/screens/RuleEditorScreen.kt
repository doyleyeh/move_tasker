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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.autosorter.data.entity.FileTypeFilter
import com.example.autosorter.data.entity.RuleEntity
import com.example.autosorter.viewmodel.RulesViewModel
import java.util.Locale
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

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
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var name by rememberSaveable { mutableStateOf(existingRule?.name ?: "") }
    var source by rememberSaveable { mutableStateOf(existingRule?.sourcePath ?: "") }
    var destination by rememberSaveable { mutableStateOf(existingRule?.destinationPath ?: "") }
    var extensions by rememberSaveable { mutableStateOf(existingRule?.extensionsFilter ?: "") }
    var enabled by rememberSaveable { mutableStateOf(existingRule?.enabled ?: true) }
    var fileType by rememberSaveable { mutableStateOf(existingRule?.fileTypeFilter ?: FileTypeFilter.IMAGE) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var isApplyingNow by rememberSaveable { mutableStateOf(false) }

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

    val canRunNow = existingRule?.id?.let { it > 0 } == true

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (ruleId == null) "Add Rule" else "Edit Rule") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = "Rule details") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Rule name") }
                )
            }
            SectionCard(title = "Folder locations") {
                DirectoryField(
                    label = "Source folder path",
                    value = source,
                    onBrowse = openSourceBrowser,
                    supportingText = "Tap to pick the folder the files start in."
                )
                Spacer(modifier = Modifier.height(12.dp))
                DirectoryField(
                    label = "Destination folder path",
                    value = destination,
                    onBrowse = openDestinationBrowser,
                    supportingText = "Where matching files should be moved."
                )
            }
            SectionCard(title = "File filters") {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = fileType.name.lowercase().replaceFirstChar { it.titlecase(Locale.ROOT) },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = { Text("File type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        FileTypeFilter.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name.lowercase().replaceFirstChar { it.titlecase(Locale.ROOT) }) },
                                onClick = {
                                    fileType = option
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
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
                    Spacer(modifier = Modifier.height(8.dp))
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
            SectionCard(title = "Status & automation") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enabled", fontWeight = FontWeight.Medium)
                        Text("Keep on to watch the folder in the background.", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val updatedRule = existingRule?.copy(
                            name = name.trim(),
                            sourcePath = source.trim(),
                            destinationPath = destination.trim(),
                            fileTypeFilter = fileType,
                            extensionsFilter = extensions.trim().ifEmpty { null },
                            enabled = enabled
                        )
                        if (updatedRule != null) {
                            isApplyingNow = true
                            viewModel.runRuleOnce(updatedRule) { moved, failed ->
                                isApplyingNow = false
                                coroutineScope.launch {
                                    val message = when {
                                        moved == 0 && failed == 0 -> "No matching files found."
                                        failed == 0 -> "Moved $moved file(s)."
                                        else -> "Moved $moved, failed $failed."
                                    }
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        }
                    },
                    enabled = canRunNow && !isApplyingNow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isApplyingNow) "Running rule..." else "Run this rule once")
                }
                if (!canRunNow) {
                    Text(
                        text = "Save the rule first to run it on existing files.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            SectionCard(title = "Actions") {
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
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && source.isNotBlank() && destination.isNotBlank()
                ) {
                    Text("Save")
                }
                TextButton(
                    onClick = onFinished,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun DirectoryField(
    label: String,
    value: String,
    onBrowse: () -> Unit,
    supportingText: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBrowse() },
        readOnly = true,
        label = { Text(label) },
        supportingText = { Text(supportingText) },
        trailingIcon = {
            IconButton(onClick = onBrowse) {
                Icon(Icons.Default.Folder, contentDescription = "Browse $label")
            }
        }
    )
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
