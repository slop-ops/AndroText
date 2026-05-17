package com.androtext.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.androtext.app.ui.viewmodel.EditorViewModel
import com.androtext.core.render.theme.EditorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        TopAppBar(
            title = { Text("Settings") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            ThemeSelector(
                themes = viewModel.availableThemes,
                currentThemeId = viewModel.currentThemeId,
                onThemeSelected = { viewModel.selectTheme(it) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Editor",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            FontSizeSetting(
                currentSize = viewModel.fontSize,
                onSizeChange = { viewModel.updateFontSize(it) },
            )

            Spacer(modifier = Modifier.height(12.dp))

            SwitchSetting(
                label = "Show Line Numbers",
                checked = viewModel.showLineNumbers,
                onCheckedChange = { viewModel.updateShowLineNumbers(it) },
            )

            Spacer(modifier = Modifier.height(12.dp))

            TabWidthSetting(
                currentWidth = viewModel.tabWidth,
                onWidthChange = { viewModel.updateTabWidth(it) },
            )

            Spacer(modifier = Modifier.height(12.dp))

            SwitchSetting(
                label = "Word Wrap",
                checked = viewModel.wordWrap,
                onCheckedChange = { viewModel.updateWordWrap(it) },
            )

            Spacer(modifier = Modifier.height(12.dp))

            SwitchSetting(
                label = "Highlight Current Line",
                checked = viewModel.highlightCurrentLine,
                onCheckedChange = { viewModel.updateHighlightCurrentLine(it) },
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ThemeSelector(
    themes: List<EditorTheme>,
    currentThemeId: String,
    onThemeSelected: (String) -> Unit,
) {
    Column {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (theme in themes) {
                ThemeCard(
                    theme = theme,
                    isSelected = theme.id == currentThemeId,
                    onClick = { onThemeSelected(theme.id) },
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: EditorTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (colorHex in theme.previewColors) {
                        val swatchColor = try {
                            Color(android.graphics.Color.parseColor(colorHex))
                        } catch (_: Exception) {
                            Color.Gray
                        }
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(swatchColor)
                                .border(
                                    BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                    RoundedCornerShape(3.dp),
                                ),
                        )
                    }
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun FontSizeSetting(
    currentSize: Float,
    onSizeChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Font Size",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${currentSize.toInt()}sp",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = currentSize,
            onValueChange = onSizeChange,
            valueRange = 8f..32f,
            steps = 23,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TabWidthSetting(
    currentWidth: Int,
    onWidthChange: (Int) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tab Width",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$currentWidth",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = currentWidth.toFloat(),
            onValueChange = { onWidthChange(it.toInt()) },
            valueRange = 2f..8f,
            steps = 5,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SwitchSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
