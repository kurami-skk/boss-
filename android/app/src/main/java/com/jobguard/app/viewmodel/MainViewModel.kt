package com.jobguard.app.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jobguard.app.analyzer.FraudDetector
import com.jobguard.app.analyzer.JobAnalyzer
import com.jobguard.app.analyzer.OcrHelper
import com.jobguard.app.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 主 ViewModel - 管理应用状态和分析逻辑
 */
class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Home)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _inputMethod = MutableStateFlow(InputMethod.TEXT)
    val inputMethod: StateFlow<InputMethod> = _inputMethod.asStateFlow()

    /**
     * 从图片分析
     */
    fun analyzeImage(bitmap: Bitmap, method: InputMethod) {
        _inputMethod.value = method
        _uiState.value = UiState.Analyzing

        viewModelScope.launch {
            try {
                // OCR 识别
                val ocrResult = withContext(Dispatchers.IO) {
                    OcrHelper.recognizeText(bitmap)
                }

                ocrResult.fold(
                    onSuccess = { text ->
                        performAnalysis(text)
                    },
                    onFailure = { error ->
                        _uiState.value = UiState.Error(error.message ?: "OCR识别失败")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "分析过程中出现未知错误")
            }
        }
    }

    /**
     * 从文本分析
     */
    fun analyzeText(text: String) {
        _inputMethod.value = InputMethod.TEXT
        _uiState.value = UiState.Analyzing

        viewModelScope.launch {
            try {
                // 延迟一小段时间让 UI 显示加载状态
                kotlinx.coroutines.delay(500)
                performAnalysis(text)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "分析过程中出现未知错误")
            }
        }
    }

    /**
     * 执行完整分析流程
     */
    private suspend fun performAnalysis(text: String) {
        withContext(Dispatchers.Default) {
            // 1. 欺诈检测
            val fraudOutput = FraudDetector.detect(text)

            // 2. 岗位内容分析
            val analysisBlocks = JobAnalyzer.analyze(text)

            // 3. 计算评分
            val (score, trustScore, transparencyScore) = FraudDetector.calculateScore(text, fraudOutput)
            val reasonScore = FraudDetector.calculateReasonableness(fraudOutput.findings.size)

            // 4. 确定风险等级
            val riskLevel = when {
                score >= 80 -> RiskLevel.SAFE
                score >= 60 -> RiskLevel.WARNING
                else -> RiskLevel.DANGER
            }

            val riskLabel = when (riskLevel) {
                RiskLevel.SAFE -> "安全可靠"
                RiskLevel.WARNING -> "需要关注"
                RiskLevel.DANGER -> "风险较高"
            }

            // 5. 生成建议
            val advice = JobAnalyzer.generateAdvice(
                score = score,
                fraudFindings = fraudOutput.findings,
                dangerCount = fraudOutput.dangerCount,
                warningCount = fraudOutput.warningCount
            )

            // 6. 组装结果
            val result = AnalysisResult(
                score = score,
                subScores = SubScores(
                    trust = trustScore,
                    transparency = transparencyScore,
                    reason = reasonScore
                ),
                fraudFindings = fraudOutput.findings,
                dangerCount = fraudOutput.dangerCount,
                warningCount = fraudOutput.warningCount,
                analysisBlocks = analysisBlocks,
                advice = advice,
                riskLevel = riskLevel,
                riskLabel = riskLabel
            )

            _uiState.value = UiState.Result(result)
        }
    }

    /**
     * 重置到首页
     */
    fun resetToHome() {
        _uiState.value = UiState.Home
    }

    override fun onCleared() {
        super.onCleared()
        OcrHelper.close()
    }
}
