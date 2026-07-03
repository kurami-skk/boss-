package com.jobguard.app

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.jobguard.app.model.InputMethod
import com.jobguard.app.model.UiState
import com.jobguard.app.ui.screens.AnalysisScreen
import com.jobguard.app.ui.screens.HomeScreen
import com.jobguard.app.ui.screens.ResultScreen
import com.jobguard.app.ui.theme.JobGuardTheme
import com.jobguard.app.viewmodel.MainViewModel
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JobGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    JobGuardApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun JobGuardApp() {
    val viewModel: MainViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val inputMethod by viewModel.inputMethod.collectAsState()

    // 状态
    var imageMode by remember { mutableStateOf(false) }
    var showAnalyzing by remember { mutableStateOf(false) }

    // 临时的 URI 用于拍照
    var photoUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // ===== 相册选择器 =====
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageMode = true
            showAnalyzing = true
            // 从 URI 读取 Bitmap
            val bitmap = try {
                val context = androidx.compose.ui.platform.LocalContext.current
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    android.graphics.BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                null
            }
            if (bitmap != null) {
                viewModel.analyzeImage(bitmap, InputMethod.GALLERY)
            } else {
                Toast.makeText(
                    androidx.compose.ui.platform.LocalContext.current,
                    "无法读取图片，请重试",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ===== 相机拍照 =====
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            imageMode = true
            showAnalyzing = true
            val bitmap = try {
                val context = androidx.compose.ui.platform.LocalContext.current
                context.contentResolver.openInputStream(photoUri!!)?.use { inputStream ->
                    android.graphics.BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                null
            }
            if (bitmap != null) {
                viewModel.analyzeImage(bitmap, InputMethod.CAMERA)
            } else {
                Toast.makeText(
                    androidx.compose.ui.platform.LocalContext.current,
                    "无法读取照片，请重试",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ===== 相机权限 =====
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    // ===== 读取存储权限 =====
    val storagePermissionState = rememberPermissionState(
        android.Manifest.permission.READ_MEDIA_IMAGES
    )

    // 获取 context
    val context = androidx.compose.ui.platform.LocalContext.current

    // 根据状态显示不同页面
    when (val state = uiState) {
        is UiState.Home -> {
            showAnalyzing = false
            HomeScreen(
                onGalleryClick = {
                    // 检查权限
                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                        if (storagePermissionState.status.isGranted) {
                            galleryLauncher.launch("image/*")
                        } else {
                            storagePermissionState.launchPermissionRequest()
                        }
                    } else {
                        galleryLauncher.launch("image/*")
                    }
                },
                onCameraClick = {
                    if (cameraPermissionState.status.isGranted) {
                        // 创建临时文件保存照片
                        val photoFile = File.createTempFile(
                            "photo_",
                            ".jpg",
                            context.cacheDir
                        )
                        photoUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        cameraLauncher.launch(photoUri!!)
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                onTextSubmit = { text ->
                    imageMode = false
                    showAnalyzing = true
                    viewModel.analyzeText(text)
                }
            )
        }

        is UiState.Analyzing -> {
            if (showAnalyzing) {
                AnalysisScreen(
                    isImageMode = imageMode,
                    onAnalysisComplete = {
                        // 分析完成后由 ViewModel 自动切换状态
                    }
                )
            }
        }

        is UiState.Result -> {
            ResultScreen(
                result = state.analysis,
                onReAnalyze = {
                    viewModel.resetToHome()
                },
                onBackHome = {
                    viewModel.resetToHome()
                },
                onShareReport = {
                    shareAnalysisResult(context, state.analysis)
                }
            )
        }

        is UiState.Error -> {
            // 显示错误并返回首页
            LaunchedEffect(state.message) {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetToHome()
            }
        }
    }
}

/**
 * 分享分析报告
 */
private fun shareAnalysisResult(
    context: android.content.Context,
    result: com.jobguard.app.model.AnalysisResult
) {
    val dangerCount = result.dangerCount
    val warningCount = result.warningCount

    val report = buildString {
        appendLine("🛡️ 求职防骗助手 - 分析报告")
        appendLine("━━━━━━━━━━━━━━━━")
        appendLine("📊 安全评分：${result.score}/100（${result.riskLabel}）")
        appendLine()
        if (dangerCount > 0 || warningCount > 0) {
            appendLine("🚨 检测到 $dangerCount 项高风险、$warningCount 项需关注特征")
        } else {
            appendLine("✅ 未检测到欺诈特征")
        }
        appendLine()
        appendLine("📱 由「求职防骗助手」生成")
        appendLine("分析结果仅供参考，请综合判断")
    }

    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, report)
        putExtra(android.content.Intent.EXTRA_SUBJECT, "求职防骗分析报告")
    }
    context.startActivity(android.content.Intent.createChooser(intent, "分享分析报告"))
}
