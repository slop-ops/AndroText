package com.androtext.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.androtext.app.ui.viewmodel.EditorTab
import com.androtext.app.ui.viewmodel.EditorViewModel
import com.androtext.app.ui.viewmodel.RecentFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onOpenFile: () -> Unit,
    onOpenRecentFile: (android.net.Uri) -> Unit,
    onSettings: () -> Unit,
    onSave: () -> Unit,
    onTabSelected: (String) -> Unit,
    onTabClosed: (String) -> Unit,
    editorContent: @Composable () -> Unit,
    previewContent: (@Composable () -> Unit)? = null,
) {
    if (viewModel.tabs.isNotEmpty()) {
        var menuExpanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (viewModel.isModified) "${viewModel.currentFileName} *" else viewModel.currentFileName ?: "",
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    actions = {
                        if (viewModel.isMarkdownFile && previewContent != null) {
                            IconButton(onClick = { viewModel.toggleMarkdownPreview() }) {
                                Icon(
                                    imageVector = if (viewModel.isMarkdownPreview) Icons.Default.Edit else Icons.Default.Visibility,
                                    contentDescription = if (viewModel.isMarkdownPreview) "Edit" else "Preview",
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Find",
                            )
                        }
                        IconButton(onClick = onSave) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save",
                            )
                        }
                        IconButton(onClick = onOpenFile) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Open file",
                            )
                        }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu",
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Find") },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.toggleSearch()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Find & Replace") },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.showReplace()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Save") },
                                    onClick = {
                                        menuExpanded = false
                                        onSave()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Save, contentDescription = null)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Close Tab") },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.activeTabId?.let { onTabClosed(it) }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = {
                                        menuExpanded = false
                                        onSettings()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Settings, contentDescription = null)
                                    },
                                )
                            }
                        }
                    },
                )

                EditorTabBar(
                    tabs = viewModel.tabs,
                    activeTabId = viewModel.activeTabId,
                    onTabSelected = onTabSelected,
                    onTabClosed = onTabClosed,
                )

                if (viewModel.isSearchOpen) {
                    SearchBar(
                        searchQuery = viewModel.searchQuery,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        replaceQuery = viewModel.replaceQuery,
                        onReplaceQueryChange = { viewModel.updateReplaceQuery(it) },
                        isReplaceMode = viewModel.isReplaceMode,
                        matchCount = viewModel.matchCount,
                        currentMatch = viewModel.currentMatch,
                        caseSensitive = viewModel.caseSensitive,
                        regex = viewModel.regexSearch,
                        requestFocus = viewModel.requestSearchFocus,
                        onFocusHandled = { viewModel.clearSearchFocusRequest() },
                        onSearchNext = { viewModel.searchNext() },
                        onSearchPrevious = { viewModel.searchPrevious() },
                        onReplace = { viewModel.performReplace() },
                        onReplaceAll = { viewModel.performReplaceAll() },
                        onToggleCaseSensitive = { viewModel.toggleCaseSensitive() },
                        onToggleRegex = { viewModel.toggleRegex() },
                        onToggleReplaceMode = { viewModel.toggleReplaceMode() },
                        onClose = { viewModel.closeSearch() },
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopStart,
                ) {
                    if (viewModel.isMarkdownPreview && viewModel.isMarkdownFile && previewContent != null) {
                        previewContent()
                    } else {
                        editorContent()
                    }
                }
            }

            if (viewModel.isLoading) {
                androidx.compose.material3.LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            WelcomeScreen(
                recentFiles = viewModel.recentFiles,
                onOpenFile = onOpenFile,
                onOpenRecentFile = onOpenRecentFile,
                onRemoveRecentFile = { uriString ->
                    viewModel.removeRecentFile(uriString)
                },
                onSettings = onSettings,
            )
            if (viewModel.isLoading) {
                androidx.compose.material3.LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                )
            }
        }
    }
}

@Composable
fun EditorTabBar(
    tabs: List<EditorTab>,
    activeTabId: String?,
    onTabSelected: (String) -> Unit,
    onTabClosed: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        tabs.forEach { tab ->
            val isActive = tab.id == activeTabId
            val bgColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            val textColor = if (isActive) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Row(
                modifier = Modifier
                    .background(bgColor)
                    .clickable { onTabSelected(tab.id) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (tab.isModified) "${tab.fileName} \u25CF" else tab.fileName,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 140.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close tab",
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onTabClosed(tab.id) },
                    tint = textColor,
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    recentFiles: List<RecentFile>,
    onOpenFile: () -> Unit,
    onOpenRecentFile: (android.net.Uri) -> Unit,
    onRemoveRecentFile: (String) -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "AndroText",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "A text editor for Android",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenFile() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Open a file",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        if (recentFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Recent Files",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(recentFiles, key = { it.uriString }) { file ->
                    RecentFileItem(
                        file = file,
                        onClick = {
                            try {
                                onOpenRecentFile(android.net.Uri.parse(file.uriString))
                            } catch (_: Exception) {
                                onRemoveRecentFile(file.uriString)
                            }
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onSettings,
            modifier = Modifier.align(Alignment.End),
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecentFileItem(
    file: RecentFile,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = file.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
