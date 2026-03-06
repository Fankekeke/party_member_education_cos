package com.fank.f1k2.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fank.f1k2.business.entity.*;
import com.fank.f1k2.business.entity.vo.UserLearningStatsVO;
import com.fank.f1k2.business.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户学习统计服务实现类
 *
 * @author FanK fan1ke2ke@gmail.com
 */
@Service
@RequiredArgsConstructor
public class UserLearningStatsServiceImpl implements IUserLearningStatsService {

    private final IUserInfoService userInfoService;
    private final IUserQuestionsService userQuestionsService;
    private final IUserLearningTraceService userLearningTraceService;
    private final IBulletinInfoService bulletinInfoService;
    private final IPartyQuestionsService partyQuestionsService;

    @Override
    public UserLearningStatsVO getUserLearningStats(Integer userId) {
        if (userId == null) {
            return null;
        }

        UserLearningStatsVO statsVO = new UserLearningStatsVO();

        // 1. 用户基本信息
        statsVO.setUserBasic(buildUserBasic(userId));

        // 2. 学习里程碑
        statsVO.setMilestone(buildLearningMilestone(userId));

        // 3. 今日推荐
        statsVO.setTodayRecommendations(buildTodayRecommendations(userId));

        // 4. 支部热榜
        statsVO.setBranchHotTopics(buildBranchHotTopics(userId));

        return statsVO;
    }

    /**
     * 构建用户基本信息
     */
    private UserLearningStatsVO.UserInfoBasic buildUserBasic(Integer userId) {
        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery()
                .eq(UserInfo::getUserId, userId));

        if (userInfo == null) {
            return null;
        }

        return new UserLearningStatsVO.UserInfoBasic()
                .setUserId(userInfo.getId())
                .setUserName(userInfo.getName())
                .setAvatar(userInfo.getImages())
                .setOrganization(userInfo.getProvince() + userInfo.getCity());
    }

    /**
     * 构建学习里程碑（游戏化成就感）
     */
    private UserLearningStatsVO.LearningMilestone buildLearningMilestone(Integer userId) {
        UserLearningStatsVO.LearningMilestone milestone = new UserLearningStatsVO.LearningMilestone();

        // 累计提问次数
        int totalQuestions = (int) userQuestionsService.count(Wrappers.<UserQuestions>lambdaQuery()
                .eq(UserQuestions::getUserId, userId));
        milestone.setTotalQuestions(totalQuestions);

        // 累计学习时长（通过足迹计算，假设每次行为平均 5 分钟）
        int totalTraces = (int) userLearningTraceService.count(Wrappers.<UserLearningTrace>lambdaQuery()
                .eq(UserLearningTrace::getUserId, userId));
        milestone.setTotalLearningMinutes(totalTraces * 5);

        // 获取用户积分
        UserInfo userInfo = userInfoService.getById(userId);
        BigDecimal integral = userInfo != null ? userInfo.getIntegral() : BigDecimal.ZERO;
        milestone.setCurrentIntegral(integral);

        // 积分等级（根据积分划分）
        String level = calculateIntegralLevel(integral);
        milestone.setIntegralLevel(level);

        // 已采纳问题数
        int adoptedQuestions = (int) userQuestionsService.count(Wrappers.<UserQuestions>lambdaQuery()
                .eq(UserQuestions::getUserId, userId)
                .eq(UserQuestions::getStatus, "已采纳"));
        milestone.setAdoptedQuestions(adoptedQuestions);

        // 等级进度（简单示例：每 100 分一级）
        int nextLevelIntegral = ((integral.intValue() / 100) + 1) * 100;
        milestone.setLevelProgress(nextLevelIntegral - integral.intValue());

        return milestone;
    }

    /**
     * 计算积分等级
     */
    private String calculateIntegralLevel(BigDecimal integral) {
        if (integral == null) {
            return "见习党员";
        }

        int points = integral.intValue();
        if (points < 100) {
            return "入党积极分子";
        } else if (points < 300) {
            return "发展对象";
        } else if (points < 500) {
            return "预备党员";
        } else if (points < 800) {
            return "正式党员";
        } else if (points < 1000) {
            return "优秀党员";
        } else {
            return "党员标兵";
        }
    }

    /**
     * 构建今日推荐（结合热点和个人兴趣）
     */
    private List<UserLearningStatsVO.RecommendationItem> buildTodayRecommendations(Integer userId) {
        List<UserLearningStatsVO.RecommendationItem> recommendations = new ArrayList<>();

        // 1. 获取用户最近的学习足迹，分析兴趣标签
        List<UserLearningTrace> recentTraces = userLearningTraceService.list(Wrappers.<UserLearningTrace>lambdaQuery()
                .eq(UserLearningTrace::getUserId, userId)
                .orderByDesc(UserLearningTrace::getCreatedAt)
                .last("LIMIT 10"));

        Set<String> userInterests = new HashSet<>();
        for (UserLearningTrace trace : recentTraces) {
            if (trace.getKeyWord() != null) {
                userInterests.add(trace.getKeyWord());
            }
        }

        // 2. 查询最近的党务公告（优先匹配兴趣）
        String todayStart = DateUtil.beginOfDay(new Date()).toString();
        String todayEnd = DateUtil.endOfDay(new Date()).toString();

        List<BulletinInfo> bulletins = bulletinInfoService.list(Wrappers.<BulletinInfo>lambdaQuery()
                .eq(BulletinInfo::getRackUp, 1)
                .ge(BulletinInfo::getDate, todayStart)
                .le(BulletinInfo::getDate, todayEnd)
                .orderByDesc(BulletinInfo::getDate)
                .last("LIMIT 3"));

        for (BulletinInfo bulletin : bulletins) {
            String matchTag = matchInterestTag(bulletin.getTitle(), bulletin.getContent(), userInterests);

            UserLearningStatsVO.RecommendationItem item = new UserLearningStatsVO.RecommendationItem()
                    .setId(bulletin.getId())
                    .setTitle(bulletin.getTitle())
                    .setSummary(bulletin.getContent().length() > 50
                            ? bulletin.getContent().substring(0, 50) + "..."
                            : bulletin.getContent())
                    .setType("BULLETIN")
                    .setPublishDate(bulletin.getDate())
                    .setMatchTag(matchTag != null ? matchTag : "党务通知");

            recommendations.add(item);
        }

        // 如果公告不足，补充热门问题
        if (recommendations.size() < 3) {
            List<PartyQuestions> hotQuestions = partyQuestionsService.list(Wrappers.<PartyQuestions>lambdaQuery()
                    .ne(PartyQuestions::getStatus, "进行中")
                    .orderByDesc(PartyQuestions::getCreatedAt)
                    .last("LIMIT " + (3 - recommendations.size())));

            for (PartyQuestions question : hotQuestions) {
                UserLearningStatsVO.RecommendationItem item = new UserLearningStatsVO.RecommendationItem()
                        .setId(question.getId())
                        .setTitle(question.getTitle())
                        .setSummary(question.getContent().length() > 50
                                ? question.getContent().substring(0, 50) + "..."
                                : question.getContent())
                        .setType("QUESTION")
                        .setPublishDate(question.getCreatedAt())
                        .setMatchTag("热点问题");

                recommendations.add(item);
            }
        }

        return recommendations;
    }

    /**
     * 匹配兴趣标签
     */
    private String matchInterestTag(String title, String content, Set<String> interests) {
        if (interests.isEmpty()) {
            return null;
        }

        String text = (title + " " + content).toLowerCase();
        for (String interest : interests) {
            if (text.contains(interest.toLowerCase())) {
                return interest;
            }
        }
        return null;
    }

    /**
     * 构建支部热榜 TOP 3
     */
    private List<UserLearningStatsVO.HotTopicItem> buildBranchHotTopics(Integer userId) {
        List<UserLearningStatsVO.HotTopicItem> hotTopics = new ArrayList<>();

        // 获取用户信息以确定所在支部
        UserInfo userInfo = userInfoService.getById(userId);
        if (userInfo == null) {
            return hotTopics;
        }

        // 统计最近 7 天的热门问题（按提问次数排序）
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        Date sevenDaysAgoDate = DateUtil.parse(sevenDaysAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        String sevenDaysAgoStr = DateUtil.formatDateTime(sevenDaysAgoDate);

        List<PartyQuestions> questions = partyQuestionsService.list(Wrappers.<PartyQuestions>lambdaQuery()
                .ge(PartyQuestions::getCreatedAt, sevenDaysAgoStr)
                .orderByDesc(PartyQuestions::getCreatedAt)
                .last("LIMIT 10"));

        // 简单热度算法：问题数量作为热度
        Map<String, Integer> topicHeatMap = new HashMap<>();
        for (PartyQuestions question : questions) {
            String title = question.getTitle();
            topicHeatMap.put(title, topicHeatMap.getOrDefault(title, 0) + 1);
        }

        // 排序取 TOP 3
        List<Map.Entry<String, Integer>> sortedTopics = topicHeatMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        int rank = 0;
        for (Map.Entry<String, Integer> topic : sortedTopics) {
            UserLearningStatsVO.HotTopicItem item = new UserLearningStatsVO.HotTopicItem()
                    .setTopicId(rank)
                    .setTitle(topic.getKey())
                    .setViewCount(topic.getValue())
                    .setHotScore(topic.getValue() * 10)
                    .setRankChange(0);

            hotTopics.add(item);
            rank++;
        }

        return hotTopics;
    }
}