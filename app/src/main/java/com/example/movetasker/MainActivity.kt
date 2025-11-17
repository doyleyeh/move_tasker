package com.example.movetasker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.movetasker.ui.navigation.MoveTaskerNavHost
import com.example.movetasker.ui.theme.MoveTaskerTheme
import com.example.movetasker.util.PermissionHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val activity = this@MainActivity
            MoveTaskerTheme {
                var permissionsGranted by remember {
                    mutableStateOf(PermissionHelper.hasAllFilesAccess(activity))
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    permissionsGranted = PermissionHelper.hasAllFilesAccess(activity)
                }

                DisposableEffect(Unit) {
                    val observer = LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            permissionsGranted = PermissionHelper.hasAllFilesAccess(activity)
                        }
                    }
                    val lifecycle = activity.lifecycle
                    lifecycle.addObserver(observer)
                    onDispose { lifecycle.removeObserver(observer) }
                }

                MoveTaskerNavHost(
                    permissionsGranted = permissionsGranted,
                    legacyPermissionsAvailable = PermissionHelper.supportsLegacyPermissions(),
                    onRequestAllFilesPermission = {
                        activity.startActivity(PermissionHelper.buildManageAllFilesIntent(activity))
                    },
                    onRequestLegacyPermissions = {
                        val permissions = PermissionHelper.legacyPermissionList()
                        if (permissions.isNotEmpty()) {
                            permissionLauncher.launch(permissions)
                        }
                    }
                )
            }
        }
    }
}
