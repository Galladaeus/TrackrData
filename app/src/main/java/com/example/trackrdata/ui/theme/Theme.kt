package com.example.trackrdata.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAccent,
    onPrimary = Color(0xFF00210E),
    primaryContainer = Color(0xFF123D25),
    onPrimaryContainer = Color(0xFFB5FFD0),

    secondary = SecondaryAccent,
    onSecondary = Color(0xFF00232B),
    secondaryContainer = Color(0xFF003943),
    onSecondaryContainer = Color(0xFFAAEFFF),

    tertiary = TertiaryAccent,
    onTertiary = Color(0xFF350018),
    tertiaryContainer = Color(0xFF52132F),
    onTertiaryContainer = Color(0xFFFFD8E7),

    background = BackgroundDark,
    onBackground = TextPrimary,

    surface = SurfaceDark,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,

    surfaceContainerLowest = Color(0xFF06090E),
    surfaceContainerLow = Color(0xFF121923),
    surfaceContainer = Color(0xFF18212D),
    surfaceContainerHigh = Color(0xFF202B39),
    surfaceContainerHighest = Color(0xFF2B3747),

    outline = Color(0xFF7C8DA3),
    outlineVariant = Color(0xFF354456),
    surfaceTint = SecondaryAccent,

    error = Color(0xFFFF6B7A),
    onError = Color(0xFF390008),
    errorContainer = Color(0xFF591522),
    onErrorContainer = Color(0xFFFFD9DF),

    inverseSurface = TextPrimary,
    inverseOnSurface = BackgroundDark,
    inversePrimary = Color(0xFF006D3B)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006D3B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB5F8CE),
    onPrimaryContainer = Color(0xFF00210E),

    secondary = Color(0xFF006878),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB5EEFF),
    onSecondaryContainer = Color(0xFF001F26),

    tertiary = Color(0xFFA51C58),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9E5),
    onTertiaryContainer = Color(0xFF3E001B),

    background = Color(0xFFF6FAF7),
    onBackground = Color(0xFF16221B),

    surface = Color(0xFFF6FAF7),
    onSurface = Color(0xFF16221B),
    onSurfaceVariant = Color(0xFF4D5E54),

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F5F1),
    surfaceContainer = Color(0xFFEAF0EC),
    surfaceContainerHigh = Color(0xFFE2EAE5),
    surfaceContainerHighest = Color(0xFFDAE4DE),

    outline = Color(0xFF6F7D74),
    outlineVariant = Color(0xFFC0CCC3),
    surfaceTint = Color(0xFF006D3B),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    inverseSurface = Color(0xFF29362E),
    inverseOnSurface = Color(0xFFEDF5EF),
    inversePrimary = PrimaryAccent
)

@Composable
fun TrackrDataTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        },
        typography = Typography,
        content = content
    )
}