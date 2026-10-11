package com.passportphotomaker.print.navigation

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.passportphotomaker.print.ui.components.GlassBottomNav
import com.passportphotomaker.print.ui.screens.CameraScreen
import com.passportphotomaker.print.ui.screens.EditorScreen
import com.passportphotomaker.print.ui.screens.HomeScreen
import com.passportphotomaker.print.ui.screens.MyPhotosScreen
import com.passportphotomaker.print.ui.screens.OnboardingScreen
import com.passportphotomaker.print.ui.screens.PreviewScreen
import com.passportphotomaker.print.ui.screens.PrintScreen
import com.passportphotomaker.print.ui.screens.SettingsScreen
import com.passportphotomaker.print.ui.screens.SizeSelectionScreen
import com.passportphotomaker.print.viewmodel.AppViewModel
import kotlinx.coroutines.launch

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CAMERA = "camera"
    const val EDITOR = "editor"
    const val SIZES = "sizes"
    const val PREVIEW = "preview"
    const val PRINT = "print"
    const val MY_PHOTOS = "myphotos"
    const val SETTINGS = "settings"
}

private val topLevelRoutes = setOf(Routes.HOME, Routes.MY_PHOTOS, Routes.PRINT, Routes.SETTINGS)

@Composable
fun AppNavigation(vm: AppViewModel) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val settings by vm.settings.collectAsStateWithLifecycle()
    val photos by vm.photos.collectAsStateWithLifecycle()
    val sourceUri by vm.sourceUri.collectAsStateWithLifecycle()
    val editedFile by vm.editedFile.collectAsStateWithLifecycle()
    val size by vm.selectedSize.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.refreshPhotos() }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val startDestination = if (settings.onboardingDone) Routes.HOME else Routes.ONBOARDING

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onGetStarted = {
                        vm.setOnboardingDone(true)
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                PaddedScreen {
                    HomeScreen(
                        onTakePhoto = { navController.navigate(Routes.CAMERA) },
                        onImagePicked = { uri ->
                            vm.setSource(uri)
                            navController.navigate(Routes.EDITOR)
                        },
                        onSeeAllSizes = { navController.navigate(Routes.SIZES) },
                        onSelectSize = { selected ->
                            vm.setSelectedSize(selected)
                            navController.navigate(Routes.CAMERA)
                        },
                        onPrint = { navController.navigate(Routes.PRINT) },
                        onMyPhotos = { navController.navigate(Routes.MY_PHOTOS) },
                        onSettings = { navController.navigate(Routes.SETTINGS) }
                    )
                }
            }

            composable(Routes.CAMERA) {
                CameraScreen(
                    showFaceGuide = settings.showFaceGuide,
                    onCaptured = { uri ->
                        vm.setSource(uri)
                        navController.navigate(Routes.EDITOR)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.EDITOR) {
                PaddedScreen {
                    EditorScreen(
                        sourceUri = sourceUri,
                        size = size,
                        onBack = { navController.popBackStack() },
                        onChangeSize = { navController.navigate(Routes.SIZES) },
                        onDone = { bitmap ->
                            scope.launch {
                                vm.saveWorking(bitmap)
                                navController.navigate(Routes.PREVIEW)
                            }
                        }
                    )
                }
            }

            composable(Routes.SIZES) {
                PaddedScreen {
                    SizeSelectionScreen(
                        selectedId = size.id,
                        onSelect = { selected ->
                            vm.setSelectedSize(selected)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Routes.PREVIEW) {
                PaddedScreen {
                    PreviewScreen(
                        editedFile = editedFile,
                        size = size,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.popBackStack() },
                        onSaveAndPrint = {
                            scope.launch {
                                val file = editedFile
                                val bitmap = file?.let {
                                    com.passportphotomaker.print.util.ImageUtils.loadBitmapFromFile(it, 2400)
                                }
                                if (bitmap != null) {
                                    val finalPhoto = vm.buildFinalPhoto(bitmap, size, 300)
                                    vm.persistPhoto(finalPhoto, size)
                                    vm.exportJpg(finalPhoto, "passport_${System.currentTimeMillis()}")
                                }
                                navController.navigate(Routes.PRINT)
                            }
                        }
                    )
                }
            }

            composable(Routes.PRINT) {
                PaddedScreen {
                    PrintScreen(
                        editedFile = editedFile,
                        size = size,
                        initialPaper = settings.defaultPaper,
                        initialQuality = settings.defaultQuality,
                        onBack = { navController.popBackStack() },
                        onNeedPhoto = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onExportPdf = { bitmap, photoSize, paper, quality, pages ->
                            scope.launch {
                                val file = vm.exportPdf(
                                    photo = bitmap,
                                    size = photoSize,
                                    paper = paper,
                                    dpi = quality.dpi,
                                    pages = pages,
                                    baseName = "passport_sheet_${System.currentTimeMillis()}"
                                )
                                shareFile(context, vm.uriFor(file), "application/pdf")
                            }
                        }
                    )
                }
            }

            composable(Routes.MY_PHOTOS) {
                PaddedScreen {
                    MyPhotosScreen(
                        photos = photos,
                        fileFor = { vm.fileFor(it) },
                        uriFor = { vm.uriFor(it) },
                        onDelete = { vm.deletePhoto(it) }
                    )
                }
            }

            composable(Routes.SETTINGS) {
                PaddedScreen {
                    SettingsScreen(
                        settings = settings,
                        onTheme = { vm.setThemeMode(it) },
                        onPaper = { vm.setDefaultPaper(it) },
                        onQuality = { vm.setDefaultQuality(it) },
                        onShowGuide = { vm.setShowFaceGuide(it) }
                    )
                }
            }
        }

        if (currentRoute in topLevelRoutes) {
            GlassBottomNav(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateTopLevel(navController, route) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun PaddedScreen(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) { content() }
}

private fun navigateTopLevel(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun shareFile(context: Context, uri: android.net.Uri, mime: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share"))
}
