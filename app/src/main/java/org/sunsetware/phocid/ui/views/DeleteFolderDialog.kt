package org.sunsetware.phocid.ui.views

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sunsetware.phocid.Dialog
import org.sunsetware.phocid.MainViewModel
import org.sunsetware.phocid.R
import org.sunsetware.phocid.globals.Strings
import org.sunsetware.phocid.ui.components.DialogBase
import org.sunsetware.phocid.utils.icuFormat

@Stable
class DeleteFolderDialog(private val folderPath: String, private val folderName: String) : Dialog() {
    @Composable
    override fun Compose(viewModel: MainViewModel) {
        val coroutineScope = rememberCoroutineScope()
        val uiManager = viewModel.uiManager

        DialogBase(
            title = Strings[R.string.folder_delete_dialog_title],
            onConfirm = {
                coroutineScope.launch {
                    val success = withContext(Dispatchers.IO) {
                        deleteFolder(File(folderPath))
                    }
                    if (success) {
                        uiManager.toast(
                            Strings[R.string.toast_folder_deleted].icuFormat(folderName)
                        )
                        viewModel.scanLibrary(true)
                    } else {
                        uiManager.toast(
                            Strings[R.string.toast_folder_delete_failed].icuFormat(folderName)
                        )
                    }
                    uiManager.closeDialog()
                }
            },
            onDismiss = { uiManager.closeDialog() },
        ) {
            Text(
                Strings[R.string.folder_delete_dialog_body].icuFormat(folderName),
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
    }
}

/**
 * Recursively delete a folder and all its contents
 * @return true if deletion was successful, false otherwise
 */
private fun deleteFolder(folder: File): Boolean {
    return try {
        if (!folder.exists()) {
            return true
        }
        
        if (folder.isDirectory) {
            // Recursively delete all child files and folders
            val children = folder.listFiles()
            if (children != null) {
                for (child in children) {
                    if (!deleteFolder(child)) {
                        return false
                    }
                }
            }
        }
        
        // Delete the folder or file itself
        folder.delete()
    } catch (e: Exception) {
        false
    }
}
