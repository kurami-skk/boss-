/**
 * Boss直聘助手 - 求职防骗分析工具
 * 核心分析引擎
 */

// ==========================================
// 欺诈检测规则库
// ==========================================
const FRAUD_RULES = [
    {
        id: 'high_salary_low_requirement',
        title: '高薪低门槛',
        severity: 'danger',
        icon: '💰',
        keywords: [
            '月入过万', '月薪过万', '轻松月入', '轻松月薪', '年薪百万',
            '日入上千', '日结*高薪', '高薪*轻松', '高薪*无经验',
            '月薪[0-9]万*无需经验', '工资日结*高薪'
        ],
        patterns: [
            { salary: 'high', experience: 'none' },
            { salary: 'high', education: 'low' }
        ],
        description: '岗位声称高薪但不需要经验或学历要求极低，这是最常见的招聘欺诈特征之一。',
        advice: '正规的高薪岗位通常对经验和能力有明确要求。建议核实公司背景，警惕"天上掉馅饼"的机会。'
    },
    {
        id: 'upfront_payment',
        title: '要求先交费',
        severity: 'danger',
        icon: '💸',
        keywords: [
            '先交', '押金', '保证金', '报名费', '培训费', '服装费',
            '体检费', '办卡费', '入会费', '建档费', '管理费',
            '预交', '垫付', '代理费', '中介费', '服务费*先',
            '收费*培训', '收费*入职', '先付', '预付'
        ],
        description: '正规招聘不会以任何名义要求求职者先支付费用。所有入职前收费都涉嫌违法。',
        advice: '根据《劳动合同法》，用人单位不得以任何名义向劳动者收取财物。遇到此类要求请立即拒绝并举报。'
    },
    {
        id: 'vague_company',
        title: '公司信息模糊',
        severity: 'warning',
        icon: '🏢',
        keywords: [
            '公司扩张', '新开分公司', '业务拓展', '大量招人',
            '急招*人数不限', '长期招聘*人数不限', '本公司直招',
            '公司直招*不通过中介', '知名企业*合作'
        ],
        description: '公司没有提供具体的公司名称、地址或业务描述，信息过于模糊。',
        advice: '建议在企查查、天眼查等平台查询公司工商信息。正规公司会有完整的注册信息和清晰的主营业务描述。'
    },
    {
        id: 'pyramid_scheme',
        title: '传销/拉人头特征',
        severity: 'danger',
        icon: '⛓️',
        keywords: [
            '拉人头', '发展下线', '推荐提成', '团队计酬',
            '多层*返利', '分级*代理', '加盟*发展',
            '直推*奖励', '团队*业绩', '无限代',
            '资本运作', '连锁经营', '家庭互助',
            '自愿连锁', '消费返利', '动态收益'
        ],
        description: '岗位描述中出现传销典型特征，如拉人头、发展下线、多层返利等。',
        advice: '传销是违法行为！如果岗位核心工作是拉人加入而非销售产品或提供服务，请立即远离并向市场监管部门举报。'
    },
    {
        id: 'training_loan',
        title: '培训贷/借贷风险',
        severity: 'danger',
        icon: '🏦',
        keywords: [
            '培训贷', '助学贷', '先培训后付款', '分期*培训',
            '信用*培训', '贷款*培训', '培训*就业*承诺',
            '包就业*培训', '保证就业*培训', '推荐就业*培训',
            '培训*上岗', '岗前培训*收费', '实训*贷款'
        ],
        description: '以培训为名诱导求职者办理贷款，承诺培训后包就业，这是典型的培训贷骗局。',
        advice: '切勿以任何形式贷款参加培训。正规公司会提供带薪培训，不会要求员工贷款支付培训费用。'
    },
    {
        id: 'unrealistic_benefits',
        title: '福利待遇夸大',
        severity: 'warning',
        icon: '🎁',
        keywords: [
            '免费*旅游', '年终*分红', '股票*期权',
            '无限*晋升', '快速*晋升', '一年*买房',
            '半年*主管', '一年*经理', '包吃包住*高薪',
            '五险一金*无需', '双休*月薪[0-9]万'
        ],
        description: '福利待遇描述过于夸张，与职位要求不匹配。',
        advice: '异常优厚的福利可能是诱饵。建议与同行业同岗位的正常待遇进行对比，理性判断。'
    },
    {
        id: 'urgent_recruitment',
        title: '急招/门槛极低',
        severity: 'warning',
        icon: '⚡',
        keywords: [
            '急招*立即上岗', '当天面试*当天入职', '无需面试',
            '来了就要', '不限*学历*经验', '无任何要求',
            '会玩手机*即可', '会打字*即可', '学生*兼职*高薪',
            '宝妈*兼职*高薪', '在家*工作*月薪'
        ],
        description: '招聘门槛极低且强调急招，不经过正常面试流程。',
        advice: '正规招聘会有完整的面试流程。过于简单快速的入职流程往往是陷阱，请提高警惕。'
    },
    {
        id: 'overseas_job',
        title: '境外/异地高薪',
        severity: 'danger',
        icon: '✈️',
        keywords: [
            '境外*高薪', '出国*工作*高薪', '海外*高薪',
            '缅甸*高薪', '柬埔寨*高薪', '迪拜*高薪',
            '东南亚*高薪', '境外*招聘', '出国*劳务',
            '海外*务工*高薪'
        ],
        description: '以高薪为诱饵招聘境外或异地工作，近年来缅甸、柬埔寨等地的电信诈骗团伙常用此手段。',
        advice: '境外高薪招聘极度危险！尤其是东南亚地区的"高薪"岗位，极有可能是电信诈骗或人口贩卖陷阱。请立即向公安机关举报。'
    },
    {
        id: 'personal_info_excess',
        title: '过度收集个人信息',
        severity: 'warning',
        icon: '🔐',
        keywords: [
            '身份证*复印件', '银行卡*号', '手持*身份证',
            '验证码*提供', '密码*提供', '支付*密码',
            '网贷*记录', '征信*报告', '担保人'
        ],
        description: '要求提供与招聘无关的过多个人信息，存在信息泄露和金融诈骗风险。',
        advice: '入职前无需提供银行卡号、密码、验证码等敏感信息。正规公司仅在办理入职手续时才会要求提供必要证件。'
    },
    {
        id: 'no_interview',
        title: '无正规面试流程',
        severity: 'warning',
        icon: '👤',
        keywords: [
            '无需面试', '线上*面试*通过', '微信*面试',
            '不用*来公司', '远程*办理*入职', '无需*到场',
            '视频面试*直接通过'
        ],
        description: '没有正规的面试流程，或者面试过程过于简单草率。',
        advice: '正规公司都会安排正式面试。跳过面试直接录用是不正常的，建议坚持参加正规的当面或视频面试。'
    },
    {
        id: 'illegal_content',
        title: '涉嫌违法违规',
        severity: 'danger',
        icon: '🚫',
        keywords: [
            '刷单', '刷信誉', '刷流水', '跑分',
            '转账*提成', '代购', '代付',
            '博彩', '赌博', '色情',
            '陪聊', '陪玩*高薪', '挂机*赚钱',
            '游戏*代练*高薪'
        ],
        description: '岗位内容涉嫌违法或违规活动，如刷单、跑分、博彩等。',
        advice: '参与违法违规活动将承担法律责任！请立即远离并向公安机关举报。'
    }
];

// ==========================================
// 岗位分析规则库
// ==========================================
const ANALYSIS_RULES = {
    salary: {
        icon: '💰',
        title: '薪资分析',
        analyze: function(text) {
            const results = [];
            // 提取薪资范围
            const salaryPatterns = [
                /(\d+)[-~-](\d+)千/g,
                /(\d+)[-~-](\d+)万/g,
                /(\d+)万以上/g,
                /(\d+)千以下/g,
                /(\d+)-(\d+)K/gi,
                /(\d+)K以上/gi,
                /面议/g,
                /薪资.*?(\d+)[-~-](\d+)/g,
                /待遇.*?(\d+)[-~-](\d+)/g
            ];

            let foundSalary = false;
            for (const pattern of salaryPatterns) {
                const match = pattern.exec(text);
                if (match) {
                    foundSalary = true;
                    if (match[0].includes('面议')) {
                        results.push({
                            type: 'info',
                            text: '薪资为面议，投递前建议先了解大概范围。'
                        });
                    } else {
                        results.push({
                            type: 'good',
                            text: `检测到薪资范围：${match[0]}。请对比同地区同岗位的正常薪资水平。`
                        });
                    }
                    break;
                }
            }

            if (!foundSalary) {
                results.push({
                    type: 'warning',
                    text: '未明确标注薪资范围，透明度较低，建议在沟通中确认。'
                });
            }

            // 检查薪资合理性
            const highSalaryKeywords = ['月入过万', '月薪过万', '年薪百万'];
            for (const kw of highSalaryKeywords) {
                if (text.includes(kw)) {
                    results.push({
                        type: 'warning',
                        text: `提到"${kw}"，如果岗位要求较低，需警惕是否属实。`
                    });
                    break;
                }
            }

            return results;
        }
    },
    requirements: {
        icon: '📋',
        title: '任职要求分析',
        analyze: function(text) {
            const results = [];

            // 学历要求
            const eduPatterns = [
                { pattern: /大专|本科|硕士|博士/, label: '有明确学历要求' },
                { pattern: /学历不限|不限学历|无学历要求/, label: '学历不限' }
            ];

            for (const edu of eduPatterns) {
                if (edu.pattern.test(text)) {
                    results.push({
                        type: edu.label === '学历不限' ? 'info' : 'good',
                        text: edu.label + (edu.label === '学历不限' ? '，包容性较强，但需结合薪资判断合理性。' : '，要求明确，透明度较好。')
                    });
                    break;
                }
            }

            // 经验要求
            const expPatterns = [
                { pattern: /经验不限|无经验|无需经验/, label: '经验不限', type: 'info' },
                { pattern: /(\d+).*年.*经验|经验.*(\d+).*年/, label: '有经验要求', type: 'good' }
            ];

            let foundExp = false;
            for (const exp of expPatterns) {
                if (exp.pattern.test(text)) {
                    results.push({
                        type: exp.type,
                        text: exp.label + (exp.label === '经验不限' ? '，适合新手入行，但请确保有相关培训机制。' : '，有助于筛选合适候选人。')
                    });
                    foundExp = true;
                    break;
                }
            }

            if (!foundExp) {
                results.push({
                    type: 'info',
                    text: '未明确说明经验要求，建议在沟通中确认。'
                });
            }

            // 技能要求
            const skillKeywords = ['熟练掌握', '精通', '熟悉', '了解', '具备.*能力', '有.*经验'];
            const foundSkills = skillKeywords.filter(s => new RegExp(s).test(text));
            if (foundSkills.length > 0) {
                results.push({
                    type: 'good',
                    text: `岗位提到了具体的技能要求（如"${foundSkills[0]}"等），有助于自我评估匹配度。`
                });
            } else {
                results.push({
                    type: 'warning',
                    text: '未列出具体的技能要求，岗位描述可能不够完整。'
                });
            }

            return results;
        }
    },
    benefits: {
        icon: '🎯',
        title: '福利待遇分析',
        analyze: function(text) {
            const results = [];

            // 五险一金
            if (/五险一金|五险|社保|公积金/.test(text)) {
                results.push({
                    type: 'good',
                    text: '✅ 明确提到五险一金/社保，这是正规公司的基本保障。'
                });
            } else {
                results.push({
                    type: 'warning',
                    text: '⚠️ 未提及五险一金/社保，正规公司通常会在岗位描述中注明。'
                });
            }

            // 其他福利
            const benefitItems = [
                { kw: '带薪年假', label: '带薪年假' },
                { kw: '年终奖', label: '年终奖' },
                { kw: '双休|周末双休', label: '双休' },
                { kw: '餐补|饭补|餐饮补贴', label: '餐饮补贴' },
                { kw: '交通补贴|交通补助', label: '交通补贴' },
                { kw: '住房补贴|住宿|包住', label: '住宿福利' },
                { kw: '定期体检|年度体检', label: '定期体检' },
                { kw: '节日福利|过节费', label: '节日福利' },
                { kw: '团建|旅游', label: '团建活动' },
                { kw: '弹性工作', label: '弹性工作制' }
            ];

            const foundBenefits = benefitItems.filter(b => new RegExp(b.kw).test(text));
            if (foundBenefits.length > 0) {
                const labels = foundBenefits.map(b => b.label).join('、');
                results.push({
                    type: 'good',
                    text: `✅ 福利待遇较完善，提到了：${labels}。`
                });
            } else {
                results.push({
                    type: 'info',
                    text: 'ℹ️ 未详细说明福利待遇，建议在面试时详细了解和确认。'
                });
            }

            return results;
        }
    },
    company: {
        icon: '🏢',
        title: '公司信息分析',
        analyze: function(text) {
            const results = [];

            // 公司规模
            const scalePatterns = [
                { pattern: /[0-9]+[-~-][0-9]+人|人数.*[0-9]+|规模.*[0-9]+/, label: '有公司规模信息' },
                { pattern: /上市公司|国企|央企|外资|合资|民营|私营/, label: '有公司类型信息' }
            ];

            let hasScaleInfo = false;
            for (const s of scalePatterns) {
                if (s.pattern.test(text)) {
                    results.push({
                        type: 'good',
                        text: `✅ ${s.label}，透明度较好。`
                    });
                    hasScaleInfo = true;
                }
            }

            if (!hasScaleInfo) {
                results.push({
                    type: 'warning',
                    text: '⚠️ 未提供公司规模和类型的明确信息，建议核实公司背景。'
                });
            }

            // 公司描述详细程度
            const detailIndicators = ['成立于', '主营', '业务', '领域', '行业', '总部', '分部'];
            const foundDetails = detailIndicators.filter(d => text.includes(d));
            if (foundDetails.length >= 2) {
                results.push({
                    type: 'good',
                    text: `✅ 公司描述较详细，提到了${foundDetails.slice(0, 3).join('、')}等信息。`
                });
            } else {
                results.push({
                    type: 'warning',
                    text: '⚠️ 公司业务描述不够详细，建议通过企查查等平台核实公司信息。'
                });
            }

            return results;
        }
    },
    development: {
        icon: '📈',
        title: '职业发展分析',
        analyze: function(text) {
            const results = [];

            const devKeywords = [
                { kw: '晋升|晋升通道|晋升机制', label: '晋升通道' },
                { kw: '培训|学习|成长|培养', label: '培训成长' },
                { kw: '职业规划|职业发展|发展空间', label: '职业发展' },
                { kw: '导师|带教|指导', label: '导师制度' }
            ];

            const foundDev = devKeywords.filter(d => new RegExp(d.kw).test(text));
            if (foundDev.length > 0) {
                const labels = foundDev.map(d => d.label).join('、');
                results.push({
                    type: 'good',
                    text: `✅ 提到了职业发展相关信息：${labels}，说明公司注重员工发展。`
                });
            } else {
                results.push({
                    type: 'info',
                    text: 'ℹ️ 未提及职业发展路径，建议面试时主动了解晋升机制和培训体系。'
                });
            }

            return results;
        }
    }
};

// ==========================================
// 综合建议库
// ==========================================
function generateAdvice(score, fraudFindings, analysisResults) {
    const advice = [];

    // 根据评分给出总体建议
    if (score >= 80) {
        advice.push({
            icon: '✅',
            text: '综合评估该岗位较为可靠，可以进一步了解。建议预约面试，实地考察公司环境和氛围。'
        });
    } else if (score >= 60) {
        advice.push({
            icon: '⚡',
            text: '该岗位存在一些需要关注的方面，建议在投递前进一步核实公司信息，面试时重点关注上述问题。'
        });
    } else {
        advice.push({
            icon: '🚨',
            text: '该岗位存在较多风险信号，强烈建议谨慎对待。优先考虑其他更可靠的职位机会。'
        });
    }

    // 根据欺诈检测结果给建议
    const dangerItems = fraudFindings.filter(f => f.severity === 'danger');
    const warningItems = fraudFindings.filter(f => f.severity === 'warning');

    if (dangerItems.length > 0) {
        advice.push({
            icon: '🚫',
            text: `检测到 ${dangerItems.length} 项高风险特征（${dangerItems.map(d => d.title).join('、')}），存在较大欺诈可能性，建议立即放弃该机会。`
        });
    }

    if (warningItems.length > 0) {
        advice.push({
            icon: '👀',
            text: `检测到 ${warningItems.length} 项需关注的特征，建议在沟通中重点核实相关信息的真实性。`
        });
    }

    // 通用建议
    advice.push({
        icon: '🔍',
        text: '建议使用企查查、天眼查等工具查询公司工商注册信息，确认公司正常经营且无不良记录。'
    });
    advice.push({
        icon: '📱',
        text: '面试前告知亲友面试地点和时间，保持手机畅通。面试过程中注意保护个人安全。'
    });
    advice.push({
        icon: '📝',
        text: '在未确认公司可靠性之前，不要提交身份证复印件、银行卡号等敏感个人信息。'
    });

    return advice;
}

// ==========================================
// 工具函数
// ==========================================

// 计算文本相似度（简单关键词匹配）
function matchKeywords(text, keywords) {
    const matches = [];
    for (const kw of keywords) {
        if (kw.includes('*')) {
            // 支持通配符匹配
            const parts = kw.split('*');
            if (parts.length === 2) {
                const escaped1 = escapeRegex(parts[0]);
                const escaped2 = escapeRegex(parts[1]);
                const regex = new RegExp(escaped1 + '.*?' + escaped2, 'i');
                if (regex.test(text)) {
                    matches.push(kw);
                }
            } else if (parts.length === 3) {
                const escaped1 = escapeRegex(parts[0]);
                const escaped2 = escapeRegex(parts[1]);
                const escaped3 = escapeRegex(parts[2]);
                const regex = new RegExp(escaped1 + '.*?' + escaped2 + '.*?' + escaped3, 'i');
                if (regex.test(text)) {
                    matches.push(kw);
                }
            }
        } else {
            const escaped = escapeRegex(kw);
            const regex = new RegExp(escaped, 'i');
            if (regex.test(text)) {
                matches.push(kw);
            }
        }
    }
    return matches;
}

// 转义正则特殊字符
function escapeRegex(str) {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

// ==========================================
// 主分析引擎
// ==========================================
function analyzeJobPosting(text) {
    // 1. 欺诈检测
    const fraudFindings = [];
    let dangerCount = 0;
    let warningCount = 0;
    let totalWeight = 0;

    for (const rule of FRAUD_RULES) {
        // 简单关键词匹配
        let foundKeywords = [];
        for (const kw of rule.keywords) {
            const matches = matchKeywords(text, [kw]);
            if (matches.length > 0) {
                foundKeywords.push(matches[0]);
            }
        }

        // 模式匹配（薪资、经验组合等）
        let patternMatch = false;
        if (rule.patterns) {
            for (const pattern of rule.patterns) {
                if (evaluatePattern(text, pattern)) {
                    patternMatch = true;
                    break;
                }
            }
        }

        if (foundKeywords.length > 0 || patternMatch) {
            const finding = {
                id: rule.id,
                title: rule.title,
                severity: rule.severity,
                icon: rule.icon,
                description: rule.description,
                advice: rule.advice,
                matchedKeywords: foundKeywords
            };
            fraudFindings.push(finding);

            if (rule.severity === 'danger') {
                dangerCount++;
                totalWeight += 30;
            } else if (rule.severity === 'warning') {
                warningCount++;
                totalWeight += 15;
            }
        }
    }

    // 2. 岗位内容分析
    const analysisBlocks = [];
    for (const [key, rule] of Object.entries(ANALYSIS_RULES)) {
        const results = rule.analyze(text);
        analysisBlocks.push({
            icon: rule.icon,
            title: rule.title,
            results: results
        });
    }

    // 3. 计算安全评分
    let score = 100;
    score -= totalWeight;
    // 文本长度加分（内容越详细越透明）
    if (text.length > 500) score += 5;
    if (text.length > 1000) score += 3;
    // 有具体结构加分
    if (text.includes('岗位职责') || text.includes('职位描述')) score += 3;
    if (text.includes('任职要求') || text.includes('岗位要求')) score += 3;
    if (text.includes('福利待遇') || text.includes('薪资福利')) score += 2;

    // 确保分数在合理范围
    score = Math.max(0, Math.min(100, score));

    // 计算子分数
    const trustScore = Math.max(0, 100 - dangerCount * 25 - warningCount * 10);
    const transparencyScore = Math.min(100, calculateTransparency(text));
    const reasonScore = Math.min(100, calculateReasonableness(text, fraudFindings));

    // 4. 生成建议
    const advice = generateAdvice(score, fraudFindings, analysisBlocks);

    return {
        score: Math.round(score),
        subScores: {
            trust: Math.round(trustScore),
            transparency: Math.round(transparencyScore),
            reason: Math.round(reasonScore)
        },
        fraudFindings: fraudFindings,
        dangerCount: dangerCount,
        warningCount: warningCount,
        analysisBlocks: analysisBlocks,
        advice: advice,
        riskLevel: score >= 80 ? 'safe' : (score >= 60 ? 'warning' : 'danger'),
        riskLabel: score >= 80 ? '安全可靠' : (score >= 60 ? '需要关注' : '风险较高')
    };
}

// 评估薪资-经验组合模式
function evaluatePattern(text, pattern) {
    if (pattern.salary === 'high' && pattern.experience === 'none') {
        const hasHighSalary = /月薪[0-9]*万|月入过万|年薪[0-9]*万/.test(text);
        const hasNoExp = /经验不限|无需经验|无经验/.test(text);
        return hasHighSalary && hasNoExp;
    }
    if (pattern.salary === 'high' && pattern.education === 'low') {
        const hasHighSalary = /月薪[0-9]*万|月入过万|年薪[0-9]*万/.test(text);
        const hasLowEdu = /学历不限|不限学历/.test(text);
        return hasHighSalary && hasLowEdu;
    }
    return false;
}

// 计算透明度
function calculateTransparency(text) {
    let score = 50;
    const factors = [
        { pattern: /岗位职责|职位描述|工作内容/, score: 8 },
        { pattern: /任职要求|岗位要求|任职资格/, score: 8 },
        { pattern: /福利待遇|薪资福利|薪酬福利/, score: 8 },
        { pattern: /公司介绍|公司简介|关于我们/, score: 6 },
        { pattern: /工作地点|上班地址|工作地址/, score: 6 },
        { pattern: /联系方式|联系人/, score: 4 },
        { pattern: /五险一金|社保/, score: 5 },
        { pattern: /学历.*大专|本科|硕士/, score: 5 }
    ];

    for (const factor of factors) {
        if (factor.pattern.test(text)) {
            score += factor.score;
        }
    }

    return Math.min(100, score);
}

// 计算合理性
function calculateReasonableness(text, fraudFindings) {
    let score = 70;
    score -= fraudFindings.length * 8;
    return Math.max(0, Math.min(100, score));
}

// ==========================================
// UI 交互逻辑
// ==========================================

// 切换输入模式
function switchTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tab === tab);
    });
    document.getElementById('tab-link').classList.toggle('hidden', tab !== 'link');
    document.getElementById('tab-text').classList.toggle('hidden', tab !== 'text');
}

// 清除输入
function clearInput(id) {
    document.getElementById(id).value = '';
    if (id === 'jobContent') updateCharCount();
}

// 更新字数统计
function updateCharCount() {
    const text = document.getElementById('jobContent').value;
    document.getElementById('charCount').textContent = text.length + ' 字';
}

// 监听文本输入
document.addEventListener('DOMContentLoaded', function() {
    const textarea = document.getElementById('jobContent');
    if (textarea) {
        textarea.addEventListener('input', updateCharCount);
    }
});

// 示例数据
function showSampleData() {
    const sampleData = `【BOSS直聘】高级运营专员

【薪资待遇】
月薪：8千-1.5万
福利：五险一金、双休、带薪年假、年终奖、定期团建、节日福利

【公司信息】
公司名称：XX科技有限公司
公司规模：100-500人
公司类型：民营
主营业务：互联网技术服务、软件开发

【职位描述】
1. 负责公司产品的日常运营和维护
2. 分析用户数据，制定运营策略
3. 策划和执行线上推广活动
4. 跟踪运营效果，持续优化方案

【任职要求】
1. 大专及以上学历，1-3年运营经验
2. 熟悉主流社交媒体平台
3. 具备良好的数据分析能力
4. 有责任心，团队协作能力强
5. 有互联网行业经验者优先

【工作地址】
北京市海淀区中关村XX大厦`;

    const linkInput = document.getElementById('jobLink');
    const textInput = document.getElementById('jobContent');

    if (!document.getElementById('tab-link').classList.contains('hidden')) {
        linkInput.value = 'https://www.zhipin.com/job_detail/示例岗位.html';
    } else {
        textInput.value = sampleData;
        updateCharCount();
    }

    showToast('✅ 已加载示例数据，点击"开始分析"查看效果');
}

// Toast 提示
function showToast(message) {
    const existing = document.querySelector('.toast');
    if (existing) existing.remove();

    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    document.body.appendChild(toast);

    requestAnimationFrame(() => {
        toast.classList.add('show');
    });

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 2500);
}

// ==========================================
// 开始分析
// ==========================================
async function startAnalysis() {
    const linkInput = document.getElementById('jobLink');
    const textInput = document.getElementById('jobContent');
    const analyzeBtn = document.getElementById('analyzeBtn');

    // 获取输入内容
    let content = '';
    const activeTab = document.querySelector('.tab-btn.active').dataset.tab;

    if (activeTab === 'link') {
        content = linkInput.value.trim();
        if (!content) {
            showToast('⚠️ 请先粘贴Boss直聘的岗位链接');
            linkInput.focus();
            return;
        }
    } else {
        content = textInput.value.trim();
        if (!content) {
            showToast('⚠️ 请先粘贴岗位介绍内容');
            textInput.focus();
            return;
        }
    }

    // 禁用按钮
    analyzeBtn.disabled = true;
    analyzeBtn.querySelector('.btn-text').textContent = '分析中...';

    // 显示加载动画
    document.getElementById('resultSection').classList.add('hidden');
    document.getElementById('loadingSection').classList.remove('hidden');

    // 模拟分析步骤动画
    await animateLoadingSteps();

    // 执行分析
    setTimeout(() => {
        const result = analyzeJobPosting(content);

        // 隐藏加载，显示结果
        document.getElementById('loadingSection').classList.add('hidden');
        document.getElementById('resultSection').classList.remove('hidden');

        // 渲染结果
        renderResults(result);

        // 恢复按钮
        analyzeBtn.disabled = false;
        analyzeBtn.querySelector('.btn-text').textContent = '开始分析';

        // 滚动到结果
        document.getElementById('resultSection').scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 600);
}

// 加载步骤动画
async function animateLoadingSteps() {
    const steps = ['step1', 'step2', 'step3', 'step4'];
    const statusTexts = ['进行中', '等待中', '等待中', '等待中'];

    for (let i = 0; i < steps.length; i++) {
        await delay(600 + Math.random() * 400);
        const stepEl = document.getElementById(steps[i]);
        const statusEl = stepEl.querySelector('.step-status');

        if (i > 0) {
            const prevStep = document.getElementById(steps[i - 1]);
            const prevStatus = prevStep.querySelector('.step-status');
            prevStatus.className = 'step-status done';
            prevStatus.textContent = '已完成';
        }

        statusEl.className = 'step-status active';
        statusEl.textContent = statusTexts[i];
    }

    // 所有步骤完成后
    await delay(400);
    for (const id of steps) {
        const stepEl = document.getElementById(id);
        const statusEl = stepEl.querySelector('.step-status');
        statusEl.className = 'step-status done';
        statusEl.textContent = '已完成';
    }
}

function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// ==========================================
// 渲染结果
// ==========================================
function renderResults(result) {
    // 1. 渲染风险评分
    renderScoreCard(result);

    // 2. 渲染欺诈检测
    renderFraudResults(result);

    // 3. 渲染岗位分析
    renderAnalysisResults(result);

    // 4. 渲染建议
    renderAdvice(result);
}

// 渲染评分卡片
function renderScoreCard(result) {
    const { score, subScores, riskLevel, riskLabel } = result;

    // 风险标签
    const badge = document.getElementById('riskBadge');
    badge.className = 'risk-badge ' + riskLevel;
    badge.querySelector('.badge-icon').textContent =
        riskLevel === 'safe' ? '✅' : (riskLevel === 'warning' ? '⚡' : '🚨');
    badge.querySelector('.badge-text').textContent = riskLabel;

    // 分数环动画
    const ring = document.getElementById('scoreRing');
    const circumference = 2 * Math.PI * 54;
    ring.style.strokeDasharray = circumference;
    const offset = circumference - (score / 100) * circumference;
    // 使用 setTimeout 触发过渡动画
    setTimeout(() => {
        ring.style.strokeDashoffset = offset;
        // 根据分数变色
        if (score >= 80) {
            ring.style.stroke = '#10B981';
        } else if (score >= 60) {
            ring.style.stroke = '#F59E0B';
        } else {
            ring.style.stroke = '#EF4444';
        }
    }, 100);

    // 数字动画
    animateNumber('scoreNumber', score, 1000);

    // 子分数条
    animateBar('trustBar', subScores.trust);
    animateBar('transparencyBar', subScores.transparency);
    animateBar('reasonBar', subScores.reason);
}

// 数字动画
function animateNumber(elementId, target, duration) {
    const el = document.getElementById(elementId);
    const start = 0;
    const startTime = performance.now();

    function update(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const eased = 1 - Math.pow(1 - progress, 3);
        const current = Math.round(start + (target - start) * eased);
        el.textContent = current;
        if (progress < 1) {
            requestAnimationFrame(update);
        }
    }
    requestAnimationFrame(update);
}

// 进度条动画
function animateBar(elementId, target) {
    const el = document.getElementById(elementId);
    setTimeout(() => {
        el.style.width = target + '%';
        if (target >= 70) {
            el.style.background = 'linear-gradient(90deg, #10B981, #34D399)';
        } else if (target >= 50) {
            el.style.background = 'linear-gradient(90deg, #F59E0B, #FBBF24)';
        } else {
            el.style.background = 'linear-gradient(90deg, #EF4444, #F87171)';
        }
    }, 200);
}

// 渲染欺诈检测
function renderFraudResults(result) {
    const container = document.getElementById('fraudResults');

    if (result.fraudFindings.length === 0) {
        container.innerHTML = `
            <div class="fraud-item safe">
                <span class="fraud-icon">✅</span>
                <div class="fraud-content">
                    <div class="fraud-title">未检测到明显欺诈特征</div>
                    <div class="fraud-desc">该岗位描述中未发现常见的招聘欺诈关键词和模式。</div>
                </div>
            </div>
        `;
        return;
    }

    let html = '';
    for (const finding of result.fraudFindings) {
        html += `
            <div class="fraud-item ${finding.severity}">
                <span class="fraud-icon">${finding.icon}</span>
                <div class="fraud-content">
                    <div class="fraud-title">${finding.severity === 'danger' ? '🚨' : '⚡'} ${finding.title}</div>
                    <div class="fraud-desc">${finding.description}</div>
                    ${finding.matchedKeywords && finding.matchedKeywords.length > 0 ? `
                        <div class="fraud-desc" style="margin-top:6px;color:var(--text-muted);font-size:12px;">
                            匹配关键词：${finding.matchedKeywords.slice(0, 5).join('、')}
                        </div>
                    ` : ''}
                    <div class="fraud-desc" style="margin-top:6px;padding:8px;background:rgba(0,0,0,0.05);border-radius:4px;font-size:13px;">
                        💡 ${finding.advice}
                    </div>
                </div>
            </div>
        `;
    }

    container.innerHTML = html;
}

// 渲染岗位分析
function renderAnalysisResults(result) {
    const container = document.getElementById('analysisResults');
    let html = '';

    for (const block of result.analysisBlocks) {
        html += `
            <div class="analysis-block">
                <h3>${block.icon} ${block.title}</h3>
                ${block.results.map(r => `
                    <p style="margin-bottom:6px;padding:6px 10px;border-radius:4px;background:${
                        r.type === 'good' ? 'rgba(16,185,129,0.08)' :
                        r.type === 'warning' ? 'rgba(245,158,11,0.08)' :
                        'rgba(59,130,246,0.08)'
                    };">${r.text}</p>
                `).join('')}
            </div>
        `;
    }

    container.innerHTML = html;
}

// 渲染建议
function renderAdvice(result) {
    const container = document.getElementById('adviceContent');
    let html = '';

    for (const item of result.advice) {
        html += `
            <div class="advice-item">
                <span class="advice-icon">${item.icon}</span>
                <p>${item.text}</p>
            </div>
        `;
    }

    container.innerHTML = html;
}

// ==========================================
// 分享功能
// ==========================================
function shareResult() {
    const score = document.getElementById('scoreNumber').textContent;
    const riskBadge = document.querySelector('.risk-badge .badge-text');
    const riskLabel = riskBadge ? riskBadge.textContent : '';

    let text = `🛡️ 求职防骗助手 - 分析报告\n`;
    text += `━━━━━━━━━━━━━━━━\n`;
    text += `📊 安全评分：${score}/100（${riskLabel}）\n\n`;

    // 添加欺诈检测摘要
    const fraudItems = document.querySelectorAll('.fraud-item');
    if (fraudItems.length > 0) {
        const dangerCount = document.querySelectorAll('.fraud-item.danger').length;
        const warningCount = document.querySelectorAll('.fraud-item.warning').length;
        if (dangerCount > 0 || warningCount > 0) {
            text += `🚨 检测到 ${dangerCount} 项高风险、${warningCount} 项需关注特征\n\n`;
        } else {
            text += `✅ 未检测到欺诈特征\n\n`;
        }
    }

    text += `📱 由「求职防骗助手」生成\n`;
    text += `分析结果仅供参考，请综合判断`;

    // 尝试使用 Web Share API
    if (navigator.share) {
        navigator.share({
            title: '求职防骗分析报告',
            text: text
        }).catch(err => {
            if (err.name !== 'AbortError') {
                fallbackCopy(text);
            }
        });
    } else {
        fallbackCopy(text);
    }
}

function fallbackCopy(text) {
    // 复制到剪贴板
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();

    try {
        document.execCommand('copy');
        showToast('📋 分析报告已复制到剪贴板，可分享给好友');
    } catch (err) {
        showToast('📤 分享功能不可用，请截图分享');
    }

    document.body.removeChild(textarea);
}

// ==========================================
// 深色模式切换
// ==========================================
document.addEventListener('DOMContentLoaded', function() {
    const themeToggle = document.getElementById('themeToggle');
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)');

    // 检查本地存储或系统偏好
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme) {
        document.documentElement.setAttribute('data-theme', savedTheme);
    } else if (prefersDark.matches) {
        document.documentElement.setAttribute('data-theme', 'dark');
    }

    themeToggle.addEventListener('click', function() {
        const current = document.documentElement.getAttribute('data-theme');
        const newTheme = current === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
    });
});

// ==========================================
// 返回顶部按钮
// ==========================================
document.addEventListener('DOMContentLoaded', function() {
    const backToTop = document.getElementById('backToTop');

    window.addEventListener('scroll', function() {
        if (window.scrollY > 600) {
            backToTop.classList.remove('hidden');
        } else {
            backToTop.classList.add('hidden');
        }
    });
});

function scrollToTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ==========================================
// PWA 注册 Service Worker
// ==========================================
if ('serviceWorker' in navigator) {
    window.addEventListener('load', function() {
        navigator.serviceWorker.register('sw.js').then(function(registration) {
            console.log('ServiceWorker 注册成功:', registration.scope);
        }, function(err) {
            console.log('ServiceWorker 注册失败:', err);
        });
    });
}

// ==========================================
// 页面加载时自动显示示例
// ==========================================
document.addEventListener('DOMContentLoaded', function() {
    // 延迟一会儿后温馨提醒
    setTimeout(() => {
        showToast('💡 点击"试试示例数据"体验分析功能');
    }, 1500);
});
