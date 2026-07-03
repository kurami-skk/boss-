package com.jobguard.app.model

/**
 * 欺诈检测结果
 */
data class FraudFinding(
    val id: String,
    val title: String,
    val severity: FraudSeverity,
    val icon: String,
    val description: String,
    val advice: String,
    val matchedKeywords: List<String> = emptyList()
)

enum class FraudSeverity {
    DANGER,
    WARNING,
    SAFE
}

/**
 * 分析块 - 岗位分析的某个方面
 */
data class AnalysisBlock(
    val icon: String,
    val title: String,
    val results: List<AnalysisItem>
)

data class AnalysisItem(
    val type: ItemType,
    val text: String
)

enum class ItemType {
    GOOD,
    WARNING,
    INFO
}

/**
 * 综合评分
 */
data class SubScores(
    val trust: Int,
    val transparency: Int,
    val reason: Int
)

/**
 * 建议项
 */
data class AdviceItem(
    val icon: String,
    val text: String
)

/**
 * 完整分析结果
 */
data class AnalysisResult(
    val score: Int,
    val subScores: SubScores,
    val fraudFindings: List<FraudFinding>,
    val dangerCount: Int,
    val warningCount: Int,
    val analysisBlocks: List<AnalysisBlock>,
    val advice: List<AdviceItem>,
    val riskLevel: RiskLevel,
    val riskLabel: String
)

enum class RiskLevel {
    SAFE,
    WARNING,
    DANGER
}

/**
 * 页面状态
 */
sealed class UiState {
    data object Home : UiState()
    data object Analyzing : UiState()
    data class Result(val analysis: AnalysisResult) : UiState()
    data class Error(val message: String) : UiState()
}

/**
 * 输入方式
 */
enum class InputMethod {
    GALLERY,
    CAMERA,
    TEXT
}
