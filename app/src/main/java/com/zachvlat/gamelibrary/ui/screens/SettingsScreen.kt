package com.zachvlat.gamelibrary.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zachvlat.gamelibrary.library.GameLibrary
import com.zachvlat.gamelibrary.library.cache.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

@Composable
fun SettingsScreen(
    library: GameLibrary,
    onDatabaseImported: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                statusMessage = null
                try {
                    exportDatabase(context, uri)
                    statusMessage = "Database exported successfully"
                } catch (e: Exception) {
                    statusMessage = "Export failed: ${e.message}"
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                statusMessage = null
                try {
                    importDatabase(context, uri)
                    library.recreateCache(context)
                    onDatabaseImported()
                    statusMessage = "Database imported successfully"
                } catch (e: Exception) {
                    statusMessage = "Import failed: ${e.message}"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "Database Backup",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "Export your game library database or import a previously saved backup.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        FilledTonalButton(
            onClick = { exportLauncher.launch("gameshelf_backup.db") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(Modifier.padding(8.dp))
            Text("Export Database")
        }

        Spacer(Modifier.height(12.dp))

        FilledTonalButton(
            onClick = { importLauncher.launch(arrayOf("application/octet-stream", "application/vnd.sqlite3")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(Modifier.padding(8.dp))
            Text("Import Database")
        }

        statusMessage?.let {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = if (it.startsWith("Error") || it.startsWith("Import failed") || it.startsWith("Export failed"))
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}

private suspend fun exportDatabase(context: Context, uri: Uri) {
    withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath("heroic_library_cache")
        if (!dbFile.exists()) throw IOException("Database file not found")

        context.contentResolver.openOutputStream(uri)?.use { output ->
            dbFile.inputStream().use { input ->
                input.copyTo(output)
            }
        } ?: throw IOException("Cannot open output stream")
    }
}

private suspend fun importDatabase(context: Context, uri: Uri) {
    withContext(Dispatchers.IO) {
        AppDatabase.closeAndClearInstance()

        val dbFile = context.getDatabasePath("heroic_library_cache")

        dbFile.parentFile?.mkdirs()

        dbFile.delete()
        File(dbFile.parentFile, "${dbFile.name}-wal").delete()
        File(dbFile.parentFile, "${dbFile.name}-shm").delete()

        context.contentResolver.openInputStream(uri)?.use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("Cannot open input stream")
    }
}
