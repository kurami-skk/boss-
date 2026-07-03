package com.jobguard.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobguard.app.model.*
import com.jobguard.app.ui.components.*
import com.jobguard.app.ui.theme.*

@Composable
fun ResultScreen(
    result: AnalysisResult,
    onReAnalyze: () -> Unit,
    onBackHome: () -> Unit,
    onShareReport: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部
        GradientHeader(
            title = "分析报告",
            subtitle = "安全评分 ${result.score}/100 · ${result.riskLabel}"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // ===== 1. 安全评分卡片 =====
            ScoreCard(result = result)

            // ===== 2. 欺诈检测结果 =====
            FraudSection(result = result)

            // ===== 3. 岗位内容解读 =====
            AnalysisSection(result = result)

            // ===== 4. 综合建议 =====
            AdviceSection(result = result)

            // ===== 5. 操作按钮 =====
            Spacer(modifier = Modifier.height(12.dp))
            GradientButton(
                text = "重新分析",
                icon = "🔄",
                onClick = onReAnalyze
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onShareReport,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📤 分享报告", fontWeight = FontWeight.Medium)
                }
                OutlinedButton(
                    onClick = onBackHome,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🏠 返回首页", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// 评分卡片
// ==========================================
@Composable
private fun ScoreCard(result: AnalysisResult) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "安全风险评估",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                RiskBadge(
                    riskLevel = result.riskLevel.name.lowercase(),
                    label = result.riskLabel
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 评分环
            ScoreRing(score = result.score)

            Spacer(modifier = Modifier.width(24.dp))

            // 子分数
            Column(modifier = Modifier.weight(1f)) {
                ScoreBar(label = "可信度", score = result.subScores.trust)
                Spacer(modifier = Modifier.height(10.dp))
                ScoreBar(label = "透明度", score = result.subScores.transparency)
                Spacer(modifier = Modifier.height(10.dp))
                ScoreBar(label = "合理性", score = result.subScores.reason)
            }
        }
    }
}

// ==========================================
// 欺诈检测区域
// ==========================================
@Composable
private fun FraudSection(result: AnalysisResult) {
    Spacer(modifier = Modifier.height(12.dp))
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🚨", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "欺诈特征检测",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (result.fraudFindings.isEmpty()) {
            SafeResultCard()
        } else {
            // 统计
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = "${result.dangerCount}", label = "高风险", color = Danger)
                StatItem(value = "${result.warningCount}", label = "需关注", color = Warning)
                StatItem(value = "${result.fraudFindings.size}", label = "总计", color = Primary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            result.fraudFindings.forEach { finding ->
                FraudResultCard(finding = finding)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==========================================
// 岗位分析区域
// ==========================================
@Composable
private fun AnalysisSection(result: AnalysisResult) {
    Spacer(modifier = Modifier.height(12.dp))
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "📋", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "岗位内容解读",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        result.analysisBlocks.forEach { block ->
            AnalysisBlockCard(block = block)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AnalysisBlockCard(block: AnalysisBlock) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = block.icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = block.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            block.results.forEach { item ->
                val bgColor = when (item.type) {
                    ItemType.GOOD -> SecondaryContainer.copy(alpha = 0.5f)
                    ItemType.WARNING -> WarningContainer.copy(alpha = 0.5f)
                    ItemType.INFO -> InfoContainer.copy(alpha = 0.5f)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = bgColor,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 建议区域
// ==========================================
@Composable
private fun AdviceSection(result: AnalysisResult) {
    Spacer(modifier = Modifier.height(12.dp))
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "💡", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "求职建议",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        result.advice.forEach { item ->
            AdviceCard(item = item)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AdviceCard(item: AdviceItem) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = PrimaryContainer.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = item.icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
