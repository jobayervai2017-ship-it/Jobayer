package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DualJsonEditorScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.SyncCutTheme
import com.example.viewmodel.EditorViewModel

enum class AppScreen {
    HOME,
    EDITOR,
    DUAL_JSON
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SyncCutTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val viewModel: EditorViewModel = viewModel()
                    SyncCutApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun SyncCutApp(viewModel: EditorViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    val projects by viewModel.projects.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                projects = projects,
                onSelectProject = { projectId ->
                    viewModel.selectProject(projectId)
                    currentScreen = AppScreen.EDITOR
                },
                onCreateProject = { title, ratio ->
                    viewModel.createNewProject(title, ratio)
                    currentScreen = AppScreen.EDITOR
                },
                onDeleteProject = { projectId ->
                    viewModel.deleteProject(projectId)
                },
                onExportZip = { projectId, onComplete ->
                    viewModel.exportProjectZip(onComplete)
                },
                onImportProjectZip = { uri ->
                    viewModel.importProjectFromZip(uri) { success ->
                        if (success) {
                            currentScreen = AppScreen.EDITOR
                        }
                    }
                }
            )
        }
        AppScreen.EDITOR -> {
            EditorScreen(
                viewModel = viewModel,
                onBack = { currentScreen = AppScreen.HOME },
                onOpenDualJson = { currentScreen = AppScreen.DUAL_JSON }
            )
        }
        AppScreen.DUAL_JSON -> {
            val proj = activeProject
            if (proj != null) {
                DualJsonEditorScreen(
                    project = proj,
                    onBack = { currentScreen = AppScreen.EDITOR },
                    onSync = { imgJson, txtJson ->
                        viewModel.syncFromJson(imgJson, txtJson)
                    }
                )
            } else {
                currentScreen = AppScreen.HOME
            }
        }
    }
}
