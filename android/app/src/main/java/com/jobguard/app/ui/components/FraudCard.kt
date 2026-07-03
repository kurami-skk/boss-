package com.jobguard.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobguard.app.model.FraudFinding
import com.jobguard.app.model.FraudSeverity
import com.jobguard.app.ui.theme.*

/**
 * 欺诈检测结果卡片（可展开查看更多信息）
 */
@Composable
fun FraudResultCard(
    finding: FraudFinding,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val (bgColor, borderColor, titleColor) = when (finding.severity) {
        FraudSeverity.DANGER -> Triple(
            DangerContainer,
            Danger.copy(alpha = 0.3f),
            Danger
        )
        FraudSeverity.WARNING -> Triple(
            WarningContainer,
            Warning.copy(alpha = 0.3f),
            Warning
        )
        FraudSeverity.SAFE -> Triple(
            SecondaryContainer,
            Secondary.copy(alpha = 0.3f),
            Secondary
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = finding.icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = finding.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor
                    )
                }
                Text(
                    text = if (expanded) "▲" else "▼",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = finding.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    if (finding.matchedKeywords.isNotEmpty()) {
                        Text(
                            text = "匹配关键词：${finding.matchedKeywords.take(5).joinToString("、")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "💡 ${finding.advice}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 无风险提示卡片
 */
@Composable
fun SafeResultCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "✅", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "未检测到明显欺诈特征",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Secondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "该岗位描述中未发现常见的招聘欺诈关键词和模式。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
