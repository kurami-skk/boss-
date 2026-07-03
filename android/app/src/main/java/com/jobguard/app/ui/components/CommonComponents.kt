package com.jobguard.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobguard.app.ui.theme.*

// ==========================================
// 渐变顶栏
// ==========================================
@Composable
fun GradientHeader(
    title: String,
    subtitle: String = ""
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
            .padding(top = 48.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🛡️",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ==========================================
// 毛玻璃卡片
// ==========================================
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

// ==========================================
// 选项卡片（首页用）
// ==========================================
@Composable
fun OptionCard(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "→",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// 评分环
// ==========================================
@Composable
fun ScoreRing(
    score: Int,
    size: Dp = 120.dp,
    strokeWidth: Dp = 8.dp
) {
    val animatedScore = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }

    val scoreColor = when {
        score >= 80 -> ScoreHigh
        score >= 60 -> ScoreMedium
        else -> ScoreLow
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val sweepAngle = (animatedScore.value / 100f) * 360f

            // 背景圆环
            drawCircle(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                style = stroke
            )

            // 进度圆环
            drawArc(
                color = scoreColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = stroke
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedScore.value.toInt()}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "分",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// 进度条
// ==========================================
@Composable
fun ScoreBar(
    label: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    val animatedWidth by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "scoreBar"
    )

    val barColor = when {
        score >= 70 -> Brush.horizontalGradient(listOf(ScoreHigh, SecondaryLight))
        score >= 50 -> Brush.horizontalGradient(listOf(ScoreMedium, Warning))
        else -> Brush.horizontalGradient(listOf(ScoreLow, Danger))
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedWidth / 100f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}

// ==========================================
// 风险标签
// ==========================================
@Composable
fun RiskBadge(
    riskLevel: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (riskLevel) {
        "safe" -> Pair(SecondaryContainer, Secondary)
        "warning" -> Pair(WarningContainer, Warning)
        else -> Pair(DangerContainer, Danger)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (riskLevel) {
                    "safe" -> "✅"
                    "warning" -> "⚡"
                    else -> "🚨"
                },
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ==========================================
// 分析步骤指示器（加载页用）
// ==========================================
@Composable
fun AnalysisStep(
    icon: String,
    text: String,
    state: StepState,
    modifier: Modifier = Modifier
) {
    val statusColor = when (state) {
        StepState.DONE -> Secondary
        StepState.ACTIVE -> Primary
        StepState.WAITING -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    val bgColor = when (state) {
        StepState.DONE -> SecondaryContainer
        StepState.ACTIVE -> PrimaryContainer
        StepState.WAITING -> MaterialTheme.colorScheme.surfaceVariant
    }

    val statusText = when (state) {
        StepState.DONE -> "已完成"
        StepState.ACTIVE -> "进行中"
        StepState.WAITING -> "等待中"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = statusColor.copy(alpha = 0.15f)
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

enum class StepState {
    WAITING, ACTIVE, DONE
}

// ==========================================
// 主按钮
// ==========================================
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            disabledContainerColor = Primary.copy(alpha = 0.5f)
        ),
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ==========================================
// 主操作按钮（带渐变）
// ==========================================
@Composable
fun GradientButton(
    text: String,
    icon: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
        ),
        enabled = enabled
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (enabled) listOf(GradientStart, GradientEnd)
                        else listOf(Color.Gray, Color.Gray)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon.isNotEmpty()) {
                Text(text = icon, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

// ==========================================
// 加载动画（旋转环）
// ==========================================
@Composable
fun LoadingSpinner(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(
                color = Primary.copy(alpha = 0.2f),
                style = stroke
            )
            drawArc(
                color = Primary,
                startAngle = rotation,
                sweepAngle = 120f,
                useCenter = false,
                style = stroke
            )
        }
        Canvas(
            modifier = Modifier
                .size(40.dp)
                .rotate(rotation * -1.5f)
        ) {
            val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = Secondary,
                startAngle = rotation,
                sweepAngle = 90f,
                useCenter = false,
                style = stroke
            )
        }
    }
}

private fun Modifier.rotate(degrees: Float): Modifier = this.then(
    androidx.compose.ui.draw.rotate(degrees)
)
