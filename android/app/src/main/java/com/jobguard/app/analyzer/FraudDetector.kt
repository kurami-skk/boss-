package com.jobguard.app.analyzer

import com.jobguard.app.model.*

/**
 * 欺诈检测引擎 - 检测招聘信息中的欺诈特征
 */
object FraudDetector {

    private val fraudRules = listOf(
        FraudRule(
            id = "high_salary_low_requirement",
            title = "高薪低门槛",
            severity = FraudSeverity.DANGER,
            icon = "💰",
            keywords = listOf(
                "月入过万", "月薪过万", "轻松月入", "轻松月薪", "年薪百万",
                "日入上千", "高薪", "工资日结"
            ),
            description = "岗位声称高薪但不需要经验或学历要求极低，这是最常见的招聘欺诈特征之一。",
            advice = "正规的高薪岗位通常对经验和能力有明确要求。建议核实公司背景，警惕\"天上掉馅饼\"的机会。"
        ),
        FraudRule(
            id = "upfront_payment",
            title = "要求先交费",
            severity = FraudSeverity.DANGER,
            icon = "💸",
            keywords = listOf(
                "先交", "押金", "保证金", "报名费", "培训费", "服装费",
                "体检费", "办卡费", "入会费", "建档费", "管理费",
                "预交", "垫付", "代理费", "中介费", "先付", "预付"
            ),
            description = "正规招聘不会以任何名义要求求职者先支付费用。所有入职前收费都涉嫌违法。",
            advice = "根据《劳动合同法》，用人单位不得以任何名义向劳动者收取财物。遇到此类要求请立即拒绝并举报。"
        ),
        FraudRule(
            id = "vague_company",
            title = "公司信息模糊",
            severity = FraudSeverity.WARNING,
            icon = "🏢",
            keywords = listOf(
                "公司扩张", "新开分公司", "业务拓展", "大量招人",
                "急招", "本公司直招", "公司直招"
            ),
            description = "公司没有提供具体的公司名称、地址或业务描述，信息过于模糊。",
            advice = "建议在企查查、天眼查等平台查询公司工商信息。正规公司会有完整的注册信息和清晰的主营业务描述。"
        ),
        FraudRule(
            id = "pyramid_scheme",
            title = "传销/拉人头特征",
            severity = FraudSeverity.DANGER,
            icon = "⛓️",
            keywords = listOf(
                "拉人头", "发展下线", "推荐提成", "团队计酬",
                "多层返利", "分级代理", "加盟发展",
                "直推奖励", "团队业绩", "无限代",
                "资本运作", "连锁经营", "家庭互助",
                "消费返利", "动态收益"
            ),
            description = "岗位描述中出现传销典型特征，如拉人头、发展下线、多层返利等。",
            advice = "传销是违法行为！如果岗位核心工作是拉人加入而非销售产品或提供服务，请立即远离并向市场监管部门举报。"
        ),
        FraudRule(
            id = "training_loan",
            title = "培训贷/借贷风险",
            severity = FraudSeverity.DANGER,
            icon = "🏦",
            keywords = listOf(
                "培训贷", "助学贷", "先培训后付款", "分期培训",
                "贷款培训", "包就业", "保证就业",
                "推荐就业", "岗前培训", "实训贷款"
            ),
            description = "以培训为名诱导求职者办理贷款，承诺培训后包就业，这是典型的培训贷骗局。",
            advice = "切勿以任何形式贷款参加培训。正规公司会提供带薪培训，不会要求员工贷款支付培训费用。"
        ),
        FraudRule(
            id = "unrealistic_benefits",
            title = "福利待遇夸大",
            severity = FraudSeverity.WARNING,
            icon = "🎁",
            keywords = listOf(
                "免费旅游", "年终分红", "股票期权",
                "无限晋升", "快速晋升", "一年买房",
                "半年主管", "一年经理", "包吃包住"
            ),
            description = "福利待遇描述过于夸张，与职位要求不匹配。",
            advice = "异常优厚的福利可能是诱饵。建议与同行业同岗位的正常待遇进行对比，理性判断。"
        ),
        FraudRule(
            id = "urgent_recruitment",
            title = "急招/门槛极低",
            severity = FraudSeverity.WARNING,
            icon = "⚡",
            keywords = listOf(
                "立即上岗", "当天面试当天入职", "无需面试",
                "不限学历", "无任何要求", "来了就要",
                "会玩手机即可", "会打字即可", "在家工作"
            ),
            description = "招聘门槛极低且强调急招，不经过正常面试流程。",
            advice = "正规招聘会有完整的面试流程。过于简单快速的入职流程往往是陷阱，请提高警惕。"
        ),
        FraudRule(
            id = "overseas_job",
            title = "境外/异地高薪",
            severity = FraudSeverity.DANGER,
            icon = "✈️",
            keywords = listOf(
                "境外高薪", "出国工作", "海外高薪",
                "缅甸", "柬埔寨", "迪拜",
                "东南亚", "境外招聘", "出国劳务",
                "海外务工"
            ),
            description = "以高薪为诱饵招聘境外或异地工作，近年来缅甸、柬埔寨等地的电信诈骗团伙常用此手段。",
            advice = "境外高薪招聘极度危险！尤其是东南亚地区的\"高薪\"岗位，极有可能是电信诈骗或人口贩卖陷阱。请立即向公安机关举报。"
        ),
        FraudRule(
            id = "personal_info_excess",
            title = "过度收集个人信息",
            severity = FraudSeverity.WARNING,
            icon = "🔐",
            keywords = listOf(
                "身份证复印件", "银行卡号", "手持身份证",
                "验证码", "支付密码",
                "网贷记录", "征信报告", "担保人"
            ),
            description = "要求提供与招聘无关的过多个人信息，存在信息泄露和金融诈骗风险。",
            advice = "入职前无需提供银行卡号、密码、验证码等敏感信息。正规公司仅在办理入职手续时才会要求提供必要证件。"
        ),
        FraudRule(
            id = "no_interview",
            title = "无正规面试流程",
            severity = FraudSeverity.WARNING,
            icon = "👤",
            keywords = listOf(
                "无需面试", "线上面试", "微信面试",
                "不用来公司", "远程办理入职", "无需到场",
                "视频面试直接通过"
            ),
            description = "没有正规的面试流程，或者面试过程过于简单草率。",
            advice = "正规公司都会安排正式面试。跳过面试直接录用是不正常的，建议坚持参加正规的当面或视频面试。"
        ),
        FraudRule(
            id = "illegal_content",
            title = "涉嫌违法违规",
            severity = FraudSeverity.DANGER,
            icon = "🚫",
            keywords = listOf(
                "刷单", "刷信誉", "刷流水", "跑分",
                "转账提成", "代购", "代付",
                "博彩", "赌博", "色情",
                "陪聊", "挂机赚钱", "游戏代练"
            ),
            description = "岗位内容涉嫌违法或违规活动，如刷单、跑分、博彩等。",
            advice = "参与违法违规活动将承担法律责任！请立即远离并向公安机关举报。"
        )
    )

    /**
     * 对文本进行欺诈检测
     */
    fun detect(text: String): FraudDetectionOutput {
        val findings = mutableListOf<FraudFinding>()
        var dangerCount = 0
        var warningCount = 0
        var totalWeight = 0

        for (rule in fraudRules) {
            val matchedKeywords = findKeywords(text, rule.keywords)

            // 额外模式检测
            val patternMatch = evaluatePatterns(text, rule.id)

            if (matchedKeywords.isNotEmpty() || patternMatch) {
                val finding = FraudFinding(
                    id = rule.id,
                    title = rule.title,
                    severity = rule.severity,
                    icon = rule.icon,
                    description = rule.description,
                    advice = rule.advice,
                    matchedKeywords = matchedKeywords
                )
                findings.add(finding)

                when (rule.severity) {
                    FraudSeverity.DANGER -> {
                        dangerCount++
                        totalWeight += 30
                    }
                    FraudSeverity.WARNING -> {
                        warningCount++
                        totalWeight += 15
                    }
                    FraudSeverity.SAFE -> {}
                }
            }
        }

        return FraudDetectionOutput(findings, dangerCount, warningCount, totalWeight)
    }

    data class FraudDetectionOutput(
        val findings: List<FraudFinding>,
        val dangerCount: Int,
        val warningCount: Int,
        val totalWeight: Int
    )

    private fun findKeywords(text: String, keywords: List<String>): List<String> {
        val matched = mutableListOf<String>()
        val lowerText = text.lowercase()
        for (kw in keywords) {
            if (lowerText.contains(kw.lowercase())) {
                matched.add(kw)
            }
        }
        return matched
    }

    private fun evaluatePatterns(text: String, ruleId: String): Boolean {
        return when (ruleId) {
            "high_salary_low_requirement" -> {
                val hasHighSalary = Regex("月薪\\d*万|月入过万|年薪\\d*万").containsMatchIn(text)
                val hasNoExp = Regex("经验不限|无需经验|无经验").containsMatchIn(text)
                hasHighSalary && hasNoExp
            }
            else -> false
        }
    }

    /**
     * 计算透明度分数
     */
    fun calculateTransparency(text: String): Int {
        var score = 50
        val factors = listOf(
            Regex("岗位职责|职位描述|工作内容") to 8,
            Regex("任职要求|岗位要求|任职资格") to 8,
            Regex("福利待遇|薪资福利|薪酬福利") to 8,
            Regex("公司介绍|公司简介|关于我们") to 6,
            Regex("工作地点|上班地址|工作地址") to 6,
            Regex("五险一金|社保") to 5,
            Regex("学历.*大专|本科|硕士") to 5
        )

        for ((pattern, points) in factors) {
            if (pattern.containsMatchIn(text)) {
                score += points
            }
        }

        return minOf(100, score)
    }

    /**
     * 计算合理性分数
     */
    fun calculateReasonableness(fraudCount: Int): Int {
        return maxOf(0, minOf(100, 70 - fraudCount * 8))
    }

    /**
     * 计算安全评分
     */
    fun calculateScore(
        text: String,
        fraudOutput: FraudDetectionOutput
    ): Triple<Int, Int, Int> {
        var score = 100 - fraudOutput.totalWeight

        // 文本长度加分
        if (text.length > 500) score += 5
        if (text.length > 1000) score += 3

        // 结构加分
        if (text.contains("岗位职责") || text.contains("职位描述")) score += 3
        if (text.contains("任职要求") || text.contains("岗位要求")) score += 3
        if (text.contains("福利待遇") || text.contains("薪资福利")) score += 2

        score = maxOf(0, minOf(100, score))

        val trustScore = maxOf(0, 100 - fraudOutput.dangerCount * 25 - fraudOutput.warningCount * 10)
        val transparencyScore = calculateTransparency(text)
        val reasonScore = calculateReasonableness(fraudOutput.findings.size)

        return Triple(score, trustScore, transparencyScore)
    }

    private data class FraudRule(
        val id: String,
        val title: String,
        val severity: FraudSeverity,
        val icon: String,
        val keywords: List<String>,
        val description: String,
        val advice: String
    )
}
