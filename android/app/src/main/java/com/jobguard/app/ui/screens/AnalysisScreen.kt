package com.jobguard.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobguard.app.ui.components.*
import com.jobguard.app.ui.theme.*

@Composable
fun AnalysisScreen(
    isImageMode: Boolean,
    onAnalysisComplete: () -> Unit
) {
    // 模拟分析进度
    var currentStep by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        // 逐步推进分析进度
        val steps = listOf(800, 700, 600, 500)
        for (i in steps.indices) {
            delay(steps[i].toLong())
            currentStep = i + 1
        }
        onAnalysisComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // 加载动画
            LoadingSpinner()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "正在智能分析...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (isImageMode) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "正在识别图片中的文字内容",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 分析步骤
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val steps = listOf(
                    Triple("📄", if (isImageMode) "识别图片中的文字" else "解析岗位内容", StepState.WAITING),
                    Triple("🔎", "欺诈特征检测", StepState.WAITING),
                    Triple("📊", "综合评分计算", StepState.WAITING),
                    Triple("💡", "生成解读报告", StepState.WAITING)
                )

                steps.forEachIndexed { index, (icon, text, _) ->
                    val state = when {
                        index < currentStep -> StepState.DONE
                        index == currentStep -> StepState.ACTIVE
                        else -> StepState.WAITING
                    }
                    AnalysisStep(
                        icon = icon,
                        text = text,
                        state = state
                    )
                }
            }
        }
    }
}
