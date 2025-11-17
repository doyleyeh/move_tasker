package com.example.autosorter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.autosorter.data.entity.RuleEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    rules: List<RuleEntity>,
    autosortEnabled: Boolean,
    onToggleAutosort: (Boolean) -> Unit,
    onAddRule: () -> Unit,
    onEditRule: (Long) -> Unit,
    onViewLogs: () -> Unit,
    onToggleRule: (RuleEntity, Boolean) -> Unit,
    onDeleteRule: (RuleEntity) -> Unit,
    permissionsGranted: Boolean,
    legacyPermissionsAvailable: Boolean,
    onRequestAllFilesPermission: () -> Unit,
    onRequestLegacyPermissions: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showPermissionDialog by rememberSaveable { mutableStateOf(!permissionsGranted) }

    LaunchedEffect(permissionsGranted) {
        if (!permissionsGranted) {
            showPermissionDialog = true
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Storage permission required to automate file moves.")
            }
        } else {
            showPermissionDialog = false
        }
    }

    val handleToggle: (Boolean) -> Unit = { enabled ->
        if (enabled && !permissionsGranted) {
            showPermissionDialog = true
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Grant access first to enable automation.")
            }
        } else {
            onToggleAutosort(enabled)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Move Tasker Rules") },
                actions = {
                    IconButton(onClick = onViewLogs) {
                        Icon(Icons.Default.History, contentDescription = "View Logs")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRule) {
                Icon(Icons.Default.Add, contentDescription = "Add rule")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Move Automation", fontWeight = FontWeight.Bold)
                        Text("Monitor folders in the background and move files that match your rules.")
                        if (!permissionsGranted) {
                            Text(
                                "Requires storage permission before enabling.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Switch(
                        checked = autosortEnabled,
                        onCheckedChange = handleToggle
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rules, key = { it.id }) { rule ->
                    RuleRow(
                        rule = rule,
                        onEditRule = { onEditRule(rule.id) },
                        onToggle = { enabled -> onToggleRule(rule, enabled) },
                        onDelete = { onDeleteRule(rule) }
                    )
                }
            }
        }
    }

    if (showPermissionDialog && !permissionsGranted) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permissions Required") },
            text = { Text("Enable storage access so Move Tasker can monitor and move files automatically.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    onRequestAllFilesPermission()
                }) {
                    Text("Setup Permissions")
                }
            },
            dismissButton = {
                Row {
                    if (legacyPermissionsAvailable) {
                        TextButton(onClick = {
                            showPermissionDialog = false
                            onRequestLegacyPermissions()
                        }) {
                            Text("Media access")
                        }
                    }
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Not now")
                    }
                }
            }
        )
    }
}

@Composable
private fun RuleRow(
    rule: RuleEntity,
    onEditRule: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditRule() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(rule.name, style = MaterialTheme.typography.titleMedium)
                    Text("${rule.sourcePath} -> ${rule.destinationPath}", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = rule.enabled, onCheckedChange = onToggle)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Filter: ${rule.fileTypeFilter} ${rule.extensionsFilter?.let { "($it)" } ?: ""}", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable { onDelete() }
                )
            }
        }
    }
}
