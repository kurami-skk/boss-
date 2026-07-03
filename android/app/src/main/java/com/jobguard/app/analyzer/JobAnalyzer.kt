package com.jobguard.app.analyzer

import com.jobguard.app.model.*

/**
 * 岗位内容分析引擎 - 分析薪资、要求、福利、公司、职业发展
 */
object JobAnalyzer {

    /**
     * 全面分析岗位内容
     */
    fun analyze(text: String): List<AnalysisBlock> {
        return listOf(
            analyzeSalary(text),
            analyzeRequirements(text),
            analyzeBenefits(text),
            analyzeCompany(text),
            analyzeDevelopment(text)
        )
    }

    /**
     * 薪资分析
     */
    private fun analyzeSalary(text: String): AnalysisBlock {
        val results = mutableListOf<AnalysisItem>()

        val salaryPatterns = listOf(
            Regex("(\\d+)[-~-](\\d+)千"),
            Regex("(\\d+)[-~-](\\d+)万"),
            Regex("(\\d+)万以上"),
            Regex("(\\d+)千以下"),
            Regex("(\\d+)-(\\d+)K", RegexOption.IGNORE_CASE),
            Regex("面议")
        )

        var foundSalary = false
        for (pattern in salaryPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                foundSalary = true
                if (match.value.contains("面议")) {
                    results.add(
                        AnalysisItem(ItemType.INFO, "薪资为面议，投递前建议先了解大概范围。")
                    )
                } else {
                    results.add(
                        AnalysisItem(ItemType.GOOD, "检测到薪资范围：${match.value}。请对比同地区同岗位的正常薪资水平。")
                    )
                }
                break
            }
        }

        if (!foundSalary) {
            results.add(
                AnalysisItem(ItemType.WARNING, "未明确标注薪资范围，透明度较低，建议在沟通中确认。")
            )
        }

        // 检查高薪关键词
        if (Regex("月入过万|月薪过万|年薪百万").containsMatchIn(text)) {
            results.add(
                AnalysisItem(ItemType.WARNING, "提到高薪承诺，如果岗位要求较低，需警惕是否属实。")
            )
        }

        return AnalysisBlock(icon = "💰", title = "薪资分析", results = results)
    }

    /**
     * 任职要求分析
     */
    private fun analyzeRequirements(text: String): AnalysisBlock {
        val results = mutableListOf<AnalysisItem>()

        // 学历
        when {
            Regex("大专|本科|硕士|博士").containsMatchIn(text) -> {
                results.add(AnalysisItem(ItemType.GOOD, "有明确学历要求，透明度较好。"))
            }
            Regex("学历不限|不限学历|无学历要求").containsMatchIn(text) -> {
                results.add(AnalysisItem(ItemType.INFO, "学历不限，包容性较强，但需结合薪资判断合理性。"))
            }
        }

        // 经验
        when {
            Regex("经验不限|无经验|无需经验").containsMatchIn(text) -> {
                results.add(AnalysisItem(ItemType.INFO, "经验不限，适合新手入行，请确保有相关培训机制。"))
            }
            Regex("\\d+.*年.*经验|经验.*\\d+.*年").containsMatchIn(text) -> {
                results.add(AnalysisItem(ItemType.GOOD, "有明确的经验要求，有助于筛选合适候选人。"))
            }
            else -> {
                results.add(AnalysisItem(ItemType.INFO, "未明确说明经验要求，建议在沟通中确认。"))
            }
        }

        // 技能
        val skillKeywords = listOf("熟练掌握", "精通", "熟悉", "了解", "具备.*能力", "有.*经验")
        val foundSkills = skillKeywords.any { Regex(it).containsMatchIn(text) }
        if (foundSkills) {
            results.add(AnalysisItem(ItemType.GOOD, "岗位提到了具体的技能要求，有助于自我评估匹配度。"))
        } else {
            results.add(AnalysisItem(ItemType.WARNING, "未列出具体的技能要求，岗位描述可能不够完整。"))
        }

        return AnalysisBlock(icon = "📋", title = "任职要求分析", results = results)
    }

    /**
     * 福利待遇分析
     */
    private fun analyzeBenefits(text: String): AnalysisBlock {
        val results = mutableListOf<AnalysisItem>()

        // 五险一金
        if (Regex("五险一金|五险|社保|公积金").containsMatchIn(text)) {
            results.add(AnalysisItem(ItemType.GOOD, "✅ 明确提到五险一金/社保，这是正规公司的基本保障。"))
        } else {
            results.add(AnalysisItem(ItemType.WARNING, "⚠️ 未提及五险一金/社保，正规公司通常会在岗位描述中注明。"))
        }

        // 其他福利
        val benefitItems = listOf(
            "带薪年假" to "带薪年假",
            "年终奖" to "年终奖",
            "双休|周末双休" to "双休",
            "餐补|饭补|餐饮补贴" to "餐饮补贴",
            "交通补贴|交通补助" to "交通补贴",
            "住房补贴|住宿|包住" to "住宿福利",
            "定期体检|年度体检" to "定期体检",
            "节日福利|过节费" to "节日福利",
            "团建|旅游" to "团建活动"
        )

        val foundBenefits = benefitItems.filter { (pattern, _) ->
            Regex(pattern).containsMatchIn(text)
        }

        if (foundBenefits.isNotEmpty()) {
            val labels = foundBenefits.joinToString("、") { it.second }
            results.add(AnalysisItem(ItemType.GOOD, "✅ 福利待遇较完善，提到了：$labels。"))
        } else {
            results.add(AnalysisItem(ItemType.INFO, "ℹ️ 未详细说明福利待遇，建议在面试时详细了解。"))
        }

        return AnalysisBlock(icon = "🎯", title = "福利待遇分析", results = results)
    }

    /**
     * 公司信息分析
     */
    private fun analyzeCompany(text: String): AnalysisBlock {
        val results = mutableListOf<AnalysisItem>()

        // 公司规模
        if (Regex("\\d+[-~-]\\d+人|人数.*\\d+|规模.*\\d+").containsMatchIn(text)) {
            results.add(AnalysisItem(ItemType.GOOD, "✅ 有公司规模信息，透明度较好。"))
        } else {
            results.add(AnalysisItem(ItemType.WARNING, "⚠️ 未提供公司规模的明确信息，建议核实公司背景。"))
        }

        // 公司类型
        if (Regex("上市公司|国企|央企|外资|合资|民营|私营").containsMatchIn(text)) {
            results.add(AnalysisItem(ItemType.GOOD, "✅ 有公司类型信息，透明度较好。"))
        }

        // 详细程度
        val detailIndicators = listOf("成立于", "主营", "业务", "领域", "行业", "总部")
        val foundDetails = detailIndicators.filter { text.contains(it) }
        if (foundDetails.size >= 2) {
            results.add(
                AnalysisItem(
                    ItemType.GOOD,
                    "✅ 公司描述较详细，提到了${foundDetails.take(3).joinToString("、")}等信息。"
                )
            )
        } else {
            results.add(
                AnalysisItem(
                    ItemType.WARNING,
                    "⚠️ 公司业务描述不够详细，建议通过企查查等平台核实公司信息。"
                )
            )
        }

        return AnalysisBlock(icon = "🏢", title = "公司信息分析", results = results)
    }

    /**
     * 职业发展分析
     */
    private fun analyzeDevelopment(text: String): AnalysisBlock {
        val results = mutableListOf<AnalysisItem>()

        val devItems = listOf(
            "晋升|晋升通道|晋升机制" to "晋升通道",
            "培训|学习|成长|培养" to "培训成长",
            "职业规划|职业发展|发展空间" to "职业发展",
            "导师|带教|指导" to "导师制度"
        )

        val foundDev = devItems.filter { (pattern, _) ->
            Regex(pattern).containsMatchIn(text)
        }

        if (foundDev.isNotEmpty()) {
            val labels = foundDev.joinToString("、") { it.second }
            results.add(
                AnalysisItem(
                    ItemType.GOOD,
                    "✅ 提到了职业发展相关信息：$labels，说明公司注重员工发展。"
                )
            )
        } else {
            results.add(
                AnalysisItem(
                    ItemType.INFO,
                    "ℹ️ 未提及职业发展路径，建议面试时主动了解晋升机制和培训体系。"
                )
            )
        }

        return AnalysisBlock(icon = "📈", title = "职业发展分析", results = results)
    }

    /**
     * 生成综合建议
     */
    fun generateAdvice(
        score: Int,
        fraudFindings: List<FraudFinding>,
        dangerCount: Int,
        warningCount: Int
    ): List<AdviceItem> {
        val advice = mutableListOf<AdviceItem>()

        // 总体建议
        advice.add(
            when {
                score >= 80 -> AdviceItem(
                    "✅",
                    "综合评估该岗位较为可靠，可以进一步了解。建议预约面试，实地考察公司环境和氛围。"
                )
                score >= 60 -> AdviceItem(
                    "⚡",
                    "该岗位存在一些需要关注的方面，建议在投递前进一步核实公司信息，面试时重点关注上述问题。"
                )
                else -> AdviceItem(
                    "🚨",
                    "该岗位存在较多风险信号，强烈建议谨慎对待。优先考虑其他更可靠的职位机会。"
                )
            }
        )

        // 欺诈相关建议
        if (dangerCount > 0) {
            val dangerTitles = fraudFindings
                .filter { it.severity == FraudSeverity.DANGER }
                .joinToString("、") { it.title }
            advice.add(
                AdviceItem(
                    "🚫",
                    "检测到 $dangerCount 项高风险特征（$dangerTitles），存在较大欺诈可能性，建议立即放弃该机会。"
                )
            )
        }

        if (warningCount > 0) {
            advice.add(
                AdviceItem(
                    "👀",
                    "检测到 $warningCount 项需关注的特征，建议在沟通中重点核实相关信息的真实性。"
                )
            )
        }

        // 通用建议
        advice.addAll(
            listOf(
                AdviceItem("🔍", "建议使用企查查、天眼查等工具查询公司工商注册信息，确认公司正常经营且无不良记录。"),
                AdviceItem("📱", "面试前告知亲友面试地点和时间，保持手机畅通。面试过程中注意保护个人安全。"),
                AdviceItem("📝", "在未确认公司可靠性之前，不要提交身份证复印件、银行卡号等敏感个人信息。")
            )
        )

        return advice
    }
}
