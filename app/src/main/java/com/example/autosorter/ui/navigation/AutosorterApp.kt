package com.example.autosorter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.autosorter.util.AutosortController
import com.example.autosorter.ui.screens.LogsScreen
import com.example.autosorter.ui.screens.RuleEditorScreen
import com.example.autosorter.ui.screens.RulesScreen
import com.example.autosorter.viewmodel.LogsViewModel
import com.example.autosorter.viewmodel.RulesViewModel

@Composable
fun MoveTaskerNavHost(
    permissionsGranted: Boolean,
    legacyPermissionsAvailable: Boolean,
    onRequestAllFilesPermission: () -> Unit,
    onRequestLegacyPermissions: () -> Unit,
    rulesViewModel: RulesViewModel = viewModel(),
    logsViewModel: LogsViewModel = viewModel()
) {
    val navController = rememberNavController()
    val rules by rulesViewModel.rules.collectAsState()
    val autosortEnabled by rulesViewModel.autosortEnabled.collectAsState()
    val logs by logsViewModel.logs.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(autosortEnabled, permissionsGranted) {
        if (autosortEnabled && permissionsGranted) {
            AutosortController.startMonitoring(context)
        } else {
            AutosortController.stopMonitoring(context)
        }
    }

    NavHost(navController = navController, startDestination = "rules") {
        composable("rules") {
            RulesScreen(
                rules = rules,
                autosortEnabled = autosortEnabled,
                onToggleAutosort = { rulesViewModel.setAutosortEnabled(it) },
                onAddRule = { navController.navigate("editor?ruleId=-1") },
                onEditRule = { id -> navController.navigate("editor?ruleId=$id") },
                onViewLogs = { navController.navigate("logs") },
                onToggleRule = { rule, enabled -> rulesViewModel.toggleRule(rule, enabled) },
                onDeleteRule = { rulesViewModel.deleteRule(it) },
                permissionsGranted = permissionsGranted,
                legacyPermissionsAvailable = legacyPermissionsAvailable,
                onRequestAllFilesPermission = onRequestAllFilesPermission,
                onRequestLegacyPermissions = onRequestLegacyPermissions
            )
        }
        composable(
            route = "editor?ruleId={ruleId}",
            arguments = listOf(navArgument("ruleId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("ruleId") ?: -1L
            RuleEditorScreen(
                ruleId = if (id == -1L) null else id,
                viewModel = rulesViewModel,
                onFinished = { navController.popBackStack() }
            )
        }
        composable("logs") {
            LogsScreen(
                logs = logs,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
