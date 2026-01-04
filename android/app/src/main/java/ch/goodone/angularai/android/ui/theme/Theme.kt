package ch.goodone.angularai.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3F51B5), // Indigo
    secondary = Color(0xFFFF4081), // Pink
    tertiary = Color(0xFF03A9F4)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3F51B5),
    secondary = Color(0xFFFF4081),
    tertiary = Color(0xFF03A9F4)
)

@Composable
fun AngularAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
