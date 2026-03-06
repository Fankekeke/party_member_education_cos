package com.fank.f1k2.business.controller;


import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fank.f1k2.business.entity.PartyQuestions;
import com.fank.f1k2.business.entity.UserInfo;
import com.fank.f1k2.business.entity.vo.*;
import com.fank.f1k2.business.service.*;
import com.fank.f1k2.common.utils.R;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 党员社区问题表 控制层
 *
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@RestController
@RequestMapping("/business/web")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebController {

    private final IUserInfoService userInfoService;

    private final IStaffInfoService staffInfoService;

    private final IAnswerRecordService answerRecordService;

    private final IPartyQuestionsService partyQuestionsService;

    private final IPartyAnswersService partyAnswersService;

    private final IUserQuestionsService userQuestionsService;

    private final IAdminStatsService adminStatsService;

    private final IUserLearningStatsService userLearningStatsService;

    private final IActivityMetricsService activityMetricsService;

    private final ITimeDistributionService timeDistributionService;

    private final IUserProfileService userProfileService;

    private final IWordCloudService wordCloudService;

    /**
     * 获取后台管理首页统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/adminHome")
    public R adminHome() {
        AdminHomeStatsVO statsVO = adminStatsService.getHomeStats();
        return R.ok(statsVO);
    }

    /**
     * 获取用户个人学习档案统计数据
     *
     * @param userId 用户 ID
     * @return 统计数据
     */
    @GetMapping("/userHome")
    public R userHome(Integer userId) {
        if (userId == null) {
            return R.error("用户 ID 不能为空");
        }

        UserLearningStatsVO statsVO = userLearningStatsService.getUserLearningStats(userId);
        if (statsVO == null || statsVO.getUserBasic() == null) {
            return R.error("未找到用户信息");
        }

        return R.ok(statsVO);
    }

    /**
     * 活跃度指标
     *
     * 📈 核心活跃度指标
     * DAU（日活跃用户数）
     * 统计今日有学习足迹或提问记录的用户数
     * 去重计算，确保准确性
     * WAU（周活跃用户数）
     * 统计过去 7 天的活跃用户
     * 用于观察中期趋势
     * MAU（月活跃用户数）
     * 统计过去 30 天的活跃用户
     * 用于长期趋势分析
     * 人均提问次数
     * 日均值 = 今日提问数 / 总用户数
     * 周均值 = 本周提问数 / 总用户数
     * 月均值 = 本月提问数 / 总用户数
     * 单次会话时长
     * 基于学习足迹计算
     * 假设每次行为代表一次会话，平均 5 分钟
     * 提供日/周/月三个维度的平均值
     *
     *
     *
     *
     * {
     *   "code": 0,
     *   "msg": "success",
     *   "data": {
     *     "dau": 156,
     *     "wau": 892,
     *     "mau": 3245,
     *     "avgQuestionsPerUser": {
     *       "dailyAvg": 0.85,
     *       "weeklyAvg": 5.92,
     *       "monthlyAvg": 23.45
     *     },
     *     "avgSessionDuration": {
     *       "dailyAvgMinutes": 12.5,
     *       "weeklyAvgMinutes": 45.8,
     *       "monthlyAvgMinutes": 180.3
     *     }
     *   }
     * }
     *
     * @return 统计数据
     */
    @GetMapping("/activityMetrics")
    public R queryActivityMetrics() {
        ActivityMetricsVO metricsVO = activityMetricsService.getActivityMetrics();
        return R.ok(metricsVO);
    }

    /**
     * 时间分布热力图
     *
     *  Heatmap
     *
     *
     {
     "code": 0,
     "msg": "success",
     "data": {
     "weekData": [
     {
     "dayOfWeek": 0,
     "dayName": "周日",
     "hourlyCounts": [2, 1, 0, 0, 1, 2, 5, 8, 12, 15, 18, 20, 22, 18, 15, 12, 10, 14, 18, 25, 30, 28, 20, 10],
     "totalCount": 306,
     "peakHours": "20:00-21:00"
     }
     ],
     "bestTimeSuggestion": {
     "bestDay": "周一",
     "bestTimeSlot": "晚间",
     "specificTimeRange": "20:00-21:00",
     "avgQuestions": 35,
     "recommendationLevel": 5
     },
     "timeSlotSummary": {
     "morningCount": 120,
     "forenoonCount": 280,
     "noonCount": 150,
     "afternoonCount": 320,
     "eveningCount": 180,
     "nightCount": 450,
     "lateNightCount": 80,
     "mostActiveTimeSlot": "晚间"
     }
     }
     }
     *
     * @return 统计数据
     */
    @GetMapping("/timeDistributionHeatmap")
    public R queryTimeDistributionHeatmap() {
        TimeDistributionHeatmapVO heatmapVO = timeDistributionService.getTimeDistributionHeatmap();
        return R.ok(heatmapVO);
    }

    /**
     * 用户个人资料
     *
     {
     "code": 0,
     "msg": "success",
     "data": {
     "ageGroupAnalysis": [
     {
     "ageGroupName": "35 岁以下",
     "totalUsers": 120,
     "totalQuestions": 580,
     "avgQuestionsPerUser": 4.83,
     "avgIntegral": 320.50,
     "activeRate": 65.00,
     "topFocusAreas": ["党的二十大精神", "党员发展流程", "组织生活会"],
     "preferredTimeSlot": "晚间"
     }
     ],
     "departmentAnalysis": [
     {
     "departmentId": 1,
     "departmentName": "第一党支部",
     "departmentType": "BRANCH",
     "memberCount": 45,
     "monthlyQuestions": 28,
     "monthlyAnswers": 25,
     "resolutionRate": 89.29,
     "avgResponseTime": 2.5,
     "activityRank": 1
     }
     ],
     "focusComparison": {
     "youngGroupFocus": [
     {
     "keyword": "党的二十大精神",
     "mentionCount": 85,
     "percentage": 14.66,
     "trend": 1
     }
     ],
     "middleGroupFocus": [],
     "seniorGroupFocus": []
     },
     "groupFeatureTags": [
     {
     "groupName": "年轻党员",
     "featureTags": ["学习积极性高", "关注时事热点", "善用 AI 工具", "提问频率高", "偏好晚间学习"],
     "groupDescription": "35 岁以下年轻党员思维活跃，善于利用新技术，对党务工作充满热情，是学习的生力军。"
     }
     ]
     }
     }
     */
    @GetMapping("/userProfile")
    public R queryUserProfile() {
        UserProfileVO profileVO = userProfileService.getUserProfile();
        return R.ok(profileVO);
    }

    /**
     * 高频词云
     *
     *
     {
     "code": 0,
     "msg": "success",
     "data": {
     "wordCloud": [
     {
     "word": "党纪学习",
     "frequency": 156,
     "weight": 10.0,
     "category": "政策热点"
     },
     {
     "word": "组织生活",
     "frequency": 128,
     "weight": 8.21,
     "category": "工作实务"
     }
     ],
     "topicRanking": [
     {
     "rank": 1,
     "keyword": "党纪学习",
     "mentionCount": 156,
     "participantCount": 89,
     "hotScore": 125,
     "trend": 1,
     "relatedQuestions": [
     "如何开展党纪学习教育？",
     "党纪学习的重点内容是什么？",
     "党纪学习的心得体会怎么写？"
     ]
     }
     ],
     "categoryAnalysis": {
     "theoryTopics": [
     {
     "topic": "党的二十大精神",
     "count": 98,
     "percentage": 15.2
     }
     ],
     "policyTopics": [
     {
     "topic": "党纪学习",
     "count": 156,
     "percentage": 24.1
     }
     ],
     "practiceTopics": [
     {
     "topic": "组织生活规范",
     "count": 87,
     "percentage": 13.5
     }
     ]
     },
     "emergingTopics": [
     {
     "keyword": "八项规定精神",
     "growthCount": 45,
     "growthRate": 280.5,
     "potentialIndex": 10
     }
     ]
     }
     }
     * @return
     */
    @GetMapping("/highFrequencyWordCloud")
    public R queryHighFrequencyWordCloud() {
        WordCloudVO wordCloudVO = wordCloudService.getWordCloudData();
        return R.ok(wordCloudVO);
    }
}
