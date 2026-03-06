package com.fank.f1k2.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fank.f1k2.business.entity.AnswerRecord;
import com.fank.f1k2.business.entity.PartyAnswers;
import com.fank.f1k2.business.entity.PartyQuestions;
import com.fank.f1k2.business.entity.UserInfo;
import com.fank.f1k2.business.entity.vo.AdminHomeStatsVO;
import com.fank.f1k2.business.service.IAdminStatsService;
import com.fank.f1k2.business.service.IAnswerRecordService;
import com.fank.f1k2.business.service.IPartyAnswersService;
import com.fank.f1k2.business.service.IPartyQuestionsService;
import com.fank.f1k2.business.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 *
 * @author FanK fan1ke2ke@gmail.com
 */
@Service
@RequiredArgsConstructor
public class AdminStatsServiceImpl implements IAdminStatsService {

    private final IPartyQuestionsService partyQuestionsService;
    private final IPartyAnswersService partyAnswersService;
    private final IAnswerRecordService answerRecordService;
    private final IUserInfoService userInfoService;

    @Override
    public AdminHomeStatsVO getHomeStats() {
        AdminHomeStatsVO statsVO = new AdminHomeStatsVO();

        String todayStart = DateUtil.beginOfDay(new Date()).toString();
        String todayEnd = DateUtil.endOfDay(new Date()).toString();

        statsVO.setTodayQuestionCount(countTodayQuestions(todayStart, todayEnd));
        statsVO.setTodayUserCount(countTodayUsers(todayStart, todayEnd));
        statsVO.setResolutionRate(calculateResolutionRate());
        statsVO.setPendingConfirmCount(countPendingConfirm());
        statsVO.setTopNegativeFeedbacks(getTopNegativeFeedbacks(5));
        statsVO.setLast7DaysTrend(getLast7DaysTrend());

        return statsVO;
    }

    private int countTodayQuestions(String todayStart, String todayEnd) {
        return (int) partyQuestionsService.count(Wrappers.<PartyQuestions>lambdaQuery()
                .ge(PartyQuestions::getCreatedAt, todayStart)
                .le(PartyQuestions::getCreatedAt, todayEnd));
    }

    private int countTodayUsers(String todayStart, String todayEnd) {
        List<UserInfo> users = userInfoService.list(Wrappers.<UserInfo>lambdaQuery()
                .ge(UserInfo::getCreateDate, todayStart)
                .le(UserInfo::getCreateDate, todayEnd));

        Set<Integer> userIds = users.stream()
                .map(UserInfo::getId)
                .collect(Collectors.toSet());

        return userIds.size();
    }

    private double calculateResolutionRate() {
        long totalQuestions = partyQuestionsService.count();

        if (totalQuestions == 0) {
            return 0.0;
        }

        long resolvedQuestions = partyQuestionsService.count(Wrappers.<PartyQuestions>lambdaQuery()
                .eq(PartyQuestions::getStatus, "已关闭"));

        return Math.round((double) resolvedQuestions / totalQuestions * 100 * 100) / 100.0;
    }

    private int countPendingConfirm() {
        long unansweredQuestions = partyQuestionsService.count(Wrappers.<PartyQuestions>lambdaQuery()
                .eq(PartyQuestions::getStatus, "进行中"));

        return (int) unansweredQuestions;
    }

    private List<AdminHomeStatsVO.FeedbackItem> getTopNegativeFeedbacks(int limit) {
        List<AdminHomeStatsVO.FeedbackItem> feedbackItems = new ArrayList<>();

        List<PartyQuestions> unansweredQuestions = partyQuestionsService.list(Wrappers.<PartyQuestions>lambdaQuery()
                .eq(PartyQuestions::getStatus, "进行中")
                .orderByDesc(PartyQuestions::getCreatedAt)
                .last("LIMIT " + limit));

        for (PartyQuestions question : unansweredQuestions) {
            AdminHomeStatsVO.FeedbackItem item = new AdminHomeStatsVO.FeedbackItem()
                    .setQuestionId(question.getId())
                    .setTitle(question.getTitle())
                    .setFeedback("问题待处理")
                    .setFeedbackDate(question.getCreatedAt());
            feedbackItems.add(item);
        }

        return feedbackItems;
    }

    private List<AdminHomeStatsVO.TrendData> getLast7DaysTrend() {
        List<AdminHomeStatsVO.TrendData> trendDataList = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = currentDate.minusDays(i);
            String dateStr = date.format(formatter);

            String dayStart = DateUtil.beginOfDay(DateUtil.parse(dateStr)).toString();
            String dayEnd = DateUtil.endOfDay(DateUtil.parse(dateStr)).toString();

            int questionCount = (int) partyQuestionsService.count(Wrappers.<PartyQuestions>lambdaQuery()
                    .ge(PartyQuestions::getCreatedAt, dayStart)
                    .le(PartyQuestions::getCreatedAt, dayEnd));

            int answerCount = (int) partyAnswersService.count(Wrappers.<PartyAnswers>lambdaQuery()
                    .ge(PartyAnswers::getCreatedAt, dayStart)
                    .le(PartyAnswers::getCreatedAt, dayEnd));

            AdminHomeStatsVO.TrendData trendData = new AdminHomeStatsVO.TrendData()
                    .setDate(dateStr)
                    .setQuestionCount(questionCount)
                    .setAnswerCount(answerCount);

            trendDataList.add(trendData);
        }

        return trendDataList;
    }
}
