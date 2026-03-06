package com.fank.f1k2.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fank.f1k2.business.entity.PartyQuestions;
import com.fank.f1k2.business.entity.UserInfo;
import com.fank.f1k2.business.entity.UserLearningTrace;
import com.fank.f1k2.business.entity.UserQuestions;
import com.fank.f1k2.business.entity.vo.ActivityMetricsVO;
import com.fank.f1k2.business.service.IActivityMetricsService;
import com.fank.f1k2.business.service.IPartyQuestionsService;
import com.fank.f1k2.business.service.IUserInfoService;
import com.fank.f1k2.business.service.IUserLearningTraceService;
import com.fank.f1k2.business.service.IUserQuestionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 活跃度统计服务实现类
 *
 * @author FanK fan1ke2ke@gmail.com
 */
@Service
@RequiredArgsConstructor
public class ActivityMetricsServiceImpl implements IActivityMetricsService {

    private final IUserInfoService userInfoService;
    private final IUserQuestionsService userQuestionsService;
    private final IUserLearningTraceService userLearningTraceService;
    private final IPartyQuestionsService partyQuestionsService;

    @Override
    public ActivityMetricsVO getActivityMetrics() {
        ActivityMetricsVO metricsVO = new ActivityMetricsVO();

        Date today = new Date();
        Date weekAgo = DateUtil.offsetDay(today, -7);
        Date monthAgo = DateUtil.offsetDay(today, -30);

        // 1. 日/周/月活跃用户数（DAU/WAU/MAU）
        metricsVO.setDau(countActiveUsers(DateUtil.beginOfDay(today), DateUtil.endOfDay(today)));
        metricsVO.setWau(countActiveUsers(weekAgo, today));
        metricsVO.setMau(countActiveUsers(monthAgo, today));

        // 2. 人均提问次数
        ActivityMetricsVO.AvgQuestionsPerUser avgQuestions = buildAvgQuestionsPerUser(today, weekAgo, monthAgo);
        metricsVO.setAvgQuestionsPerUser(avgQuestions);

        // 3. 单次会话时长
        ActivityMetricsVO.AvgSessionDuration avgSessionDuration = buildAvgSessionDuration(today, weekAgo, monthAgo);
        metricsVO.setAvgSessionDuration(avgSessionDuration);

        return metricsVO;
    }

    /**
     * 统计活跃用户数（有学习足迹或提问记录的用户）
     */
    private int countActiveUsers(Date startDate, Date endDate) {
        String startStr = DateUtil.formatDateTime(startDate);
        String endStr = DateUtil.formatDateTime(endDate);

        List<UserLearningTrace> traces = userLearningTraceService.list(Wrappers.<UserLearningTrace>lambdaQuery()
                .ge(UserLearningTrace::getCreatedAt, startStr)
                .le(UserLearningTrace::getCreatedAt, endStr));

        Set<Integer> userIds = traces.stream()
                .map(UserLearningTrace::getUserId)
                .collect(Collectors.toSet());

        List<UserQuestions> questions = userQuestionsService.list(Wrappers.<UserQuestions>lambdaQuery()
                .ge(UserQuestions::getCreatedAt, startStr)
                .le(UserQuestions::getCreatedAt, endStr));

        questions.stream()
                .map(UserQuestions::getUserId)
                .forEach(userIds::add);

        return userIds.size();
    }

    /**
     * 构建人均提问次数
     */
    private ActivityMetricsVO.AvgQuestionsPerUser buildAvgQuestionsPerUser(Date today, Date weekAgo, Date monthAgo) {
        ActivityMetricsVO.AvgQuestionsPerUser avgQuestions = new ActivityMetricsVO.AvgQuestionsPerUser();

        String todayStart = DateUtil.formatDateTime(DateUtil.beginOfDay(today));
        String todayEnd = DateUtil.formatDateTime(DateUtil.endOfDay(today));
        String weekStart = DateUtil.formatDateTime(weekAgo);
        String monthStart = DateUtil.formatDateTime(monthAgo);

        long totalUsers = userInfoService.count();
        if (totalUsers == 0) {
            avgQuestions.setDailyAvg(0.0).setWeeklyAvg(0.0).setMonthlyAvg(0.0);
            return avgQuestions;
        }

        long dailyQuestions = userQuestionsService.count(Wrappers.<UserQuestions>lambdaQuery()
                .ge(UserQuestions::getCreatedAt, todayStart)
                .le(UserQuestions::getCreatedAt, todayEnd));

        long weeklyQuestions = userQuestionsService.count(Wrappers.<UserQuestions>lambdaQuery()
                .ge(UserQuestions::getCreatedAt, weekStart)
                .le(UserQuestions::getCreatedAt, todayEnd));

        long monthlyQuestions = userQuestionsService.count(Wrappers.<UserQuestions>lambdaQuery()
                .ge(UserQuestions::getCreatedAt, monthStart)
                .le(UserQuestions::getCreatedAt, todayEnd));

        avgQuestions.setDailyAvg(round(dailyQuestions * 1.0 / totalUsers, 2));
        avgQuestions.setWeeklyAvg(round(weeklyQuestions * 1.0 / totalUsers, 2));
        avgQuestions.setMonthlyAvg(round(monthlyQuestions * 1.0 / totalUsers, 2));

        return avgQuestions;
    }

    /**
     * 构建单次会话时长（基于学习足迹计算）
     * 假设每次行为代表一次会话，平均每次会话 5 分钟
     */
    private ActivityMetricsVO.AvgSessionDuration buildAvgSessionDuration(Date today, Date weekAgo, Date monthAgo) {
        ActivityMetricsVO.AvgSessionDuration avgDuration = new ActivityMetricsVO.AvgSessionDuration();

        String todayStart = DateUtil.formatDateTime(DateUtil.beginOfDay(today));
        String todayEnd = DateUtil.formatDateTime(DateUtil.endOfDay(today));
        String weekStart = DateUtil.formatDateTime(weekAgo);
        String monthStart = DateUtil.formatDateTime(monthAgo);

        long dailyTraces = userLearningTraceService.count(Wrappers.<UserLearningTrace>lambdaQuery()
                .ge(UserLearningTrace::getCreatedAt, todayStart)
                .le(UserLearningTrace::getCreatedAt, todayEnd));

        long weeklyTraces = userLearningTraceService.count(Wrappers.<UserLearningTrace>lambdaQuery()
                .ge(UserLearningTrace::getCreatedAt, weekStart)
                .le(UserLearningTrace::getCreatedAt, todayEnd));

        long monthlyTraces = userLearningTraceService.count(Wrappers.<UserLearningTrace>lambdaQuery()
                .ge(UserLearningTrace::getCreatedAt, monthStart)
                .le(UserLearningTrace::getCreatedAt, todayEnd));

        int dau = countActiveUsers(DateUtil.beginOfDay(today), DateUtil.endOfDay(today));
        int wau = countActiveUsers(weekAgo, today);
        int mau = countActiveUsers(monthAgo, today);

        avgDuration.setDailyAvgMinutes(dau > 0 ? round(dailyTraces * 5.0 / dau, 2) : 0.0);
        avgDuration.setWeeklyAvgMinutes(wau > 0 ? round(weeklyTraces * 5.0 / wau, 2) : 0.0);
        avgDuration.setMonthlyAvgMinutes(mau > 0 ? round(monthlyTraces * 5.0 / mau, 2) : 0.0);

        return avgDuration;
    }

    /**
     * 保留指定小数位
     */
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        return Math.round(value * factor) / (double) factor;
    }
}