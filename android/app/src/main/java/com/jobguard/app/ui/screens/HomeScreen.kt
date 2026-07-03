package com.jobguard.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobguard.app.ui.components.*
import com.jobguard.app.ui.theme.*

@Composable
fun HomeScreen(
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onTextSubmit: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 渐变头部
        GradientHeader(
            title = "求职防骗助手",
            subtitle = "智能分析Boss直聘岗位 · 识别欺诈 · 解读职位内容"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 标题
            Text(
                text = "选择分析方式",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "从相册、拍照或粘贴文本开始分析",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 方式一：相册
            OptionCard(
                icon = "🖼️",
                title = "从相册选择截图",
                description = "从手机相册选取Boss直聘的岗位截图",
                onClick = onGalleryClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 方式二：拍照
            OptionCard(
                icon = "📸",
                title = "拍照识别",
                description = "用相机拍摄电脑或手机上的招聘信息",
                onClick = onCameraClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 方式三：粘贴文本
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📝", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "粘贴文本分析",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "复制粘贴岗位介绍文本进行分析",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 文本输入框
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            "请粘贴Boss直聘的岗位介绍内容…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    trailingIcon = {
                        if (textInput.isNotEmpty()) {
                            IconButton(onClick = { textInput = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "清除")
                            }
                        }
                    },
                    minLines = 4,
                    maxLines = 8
                )

                Spacer(modifier = Modifier.height(12.dp))

                GradientButton(
                    text = "开始分析",
                    icon = "🔍",
                    enabled = textInput.isNotBlank(),
                    onClick = {
                        focusManager.clearFocus()
                        onTextSubmit(textInput.trim())
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 使用提示
            GlassCard {
                Text(
                    text = "💡 使用提示",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                val tips = listOf(
                    "在Boss直聘App中打开岗位 → 分享 → 复制链接",
                    "或全选复制岗位介绍的全部文本内容",
                    "截图请确保清晰完整，包含薪资、要求等信息",
                    "所有数据仅在本地分析，不会上传到任何服务器"
                )
                tips.forEachIndexed { index, tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(20.dp)
                        )
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 免责声明
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WarningContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "⚠️ 分析结果仅供参考，不构成专业法律意见。如遇可疑招聘请及时向平台举报或报警。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
