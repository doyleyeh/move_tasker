package com.example.movetasker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = Color(0xFF00C897),
    onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF00513C),
    onPrimaryContainer = Color(0xFF66FFD2),
    secondary = Color(0xFFBACEC9),
    onSecondary = Color(0xFF243431),
    background = Color(0xFF0F1513),
    surface = Color(0xFF0F1513),
    surfaceVariant = Color(0xFF3F4945),
    onSurface = Color(0xFFDEE3E0),
    outline = Color(0xFF88938F)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF006E5B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF81F8D3),
    onPrimaryContainer = Color(0xFF002019),
    secondary = Color(0xFF4A635F),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFBFDF9),
    surface = Color(0xFFFBFDF9),
    surfaceVariant = Color(0xFFDCE5E1),
    onSurface = Color(0xFF1A1C1A),
    outline = Color(0xFF6F7975)
)

@Composable
fun MoveTaskerTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
