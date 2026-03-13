package com.fank.f1k2.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fank.f1k2.business.entity.*;
import com.fank.f1k2.business.entity.vo.UserProfileVO;
import com.fank.f1k2.business.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户画像服务实现类
 *
 * @author FanK fan1ke2ke@gmail.com
 */
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements IUserProfileService {

    private final IUserInfoService userInfoService;
    private final IUserQuestionsService userQuestionsService;
    private final IStaffInfoService staffInfoService;
    private final IPartyQuestionsService partyQuestionsService;

    @Override
    public UserProfileVO getUserProfile() {
        UserProfileVO profileVO = new UserProfileVO();

        List<UserInfo> allUsers = userInfoService.list();
        if (allUsers.isEmpty()) {
            return buildEmptyProfile();
        }

        Map<String, List<UserInfo>> ageGroupMap = divideUsersByAgeGroup(allUsers);

        profileVO.setAgeGroupAnalysis(buildAgeGroupAnalysis(ageGroupMap));
        profileVO.setDepartmentAnalysis(buildDepartmentAnalysis());
        profileVO.setFocusComparison(buildFocusComparison(ageGroupMap));
        profileVO.setGroupFeatureTags(buildGroupFeatureTags(ageGroupMap));

        return profileVO;
    }

    /**
     * 构建空的用户画像
     */
    private UserProfileVO buildEmptyProfile() {
        return new UserProfileVO();
    }

    /**
     * 按年龄段分组用户
     */
    private Map<String, List<UserInfo>> divideUsersByAgeGroup(List<UserInfo> users) {
        Map<String, List<UserInfo>> ageGroupMap = new LinkedHashMap<>();
        ageGroupMap.put("young", new ArrayList<>());
        ageGroupMap.put("middle", new ArrayList<>());
        ageGroupMap.put("senior", new ArrayList<>());

        LocalDate now = LocalDate.now();

        for (UserInfo user : users) {
            int age = calculateUserAge(user, now);

            if (age < 35) {
                ageGroupMap.get("young").add(user);
            } else if (age >= 35 && age < 50) {
                ageGroupMap.get("middle").add(user);
            } else {
                ageGroupMap.get("senior").add(user);
            }
        }

        return ageGroupMap;
    }

    /**
     * 计算用户年龄
     */
    private int calculateUserAge(UserInfo user, LocalDate now) {
        try {
            if (user.getBirthday() != null && !user.getBirthday().isEmpty()) {
                LocalDate birthDate = LocalDate.parse(user.getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return Period.between(birthDate, now).getYears();
            }
        } catch (Exception e) {
        }

        if (user.getCreateDate() != null && !user.getCreateDate().isEmpty()) {
            try {
                LocalDate createDate = LocalDate.parse(user.getCreateDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return Period.between(createDate, now).getYears();
            } catch (Exception e) {
            }
        }

        return 35;
    }

    /**
     * 构建年龄段分析
     */
    private List<UserProfileVO.AgeGroupAnalysis> buildAgeGroupAnalysis(Map<String, List<UserInfo>> ageGroupMap) {
        List<UserProfileVO.AgeGroupAnalysis> analysisList = new ArrayList<>();

        Map<String, String> groupNames = new LinkedHashMap<>();
        groupNames.put("young", "35 岁以下");
        groupNames.put("middle", "35-50 岁");
        groupNames.put("senior", "50 岁以上");

        for (Map.Entry<String, List<UserInfo>> entry : ageGroupMap.entrySet()) {
            String groupKey = entry.getKey();
            List<UserInfo> groupUsers = entry.getValue();

            if (groupUsers.isEmpty()) {
                continue;
            }

            UserProfileVO.AgeGroupAnalysis analysis = new UserProfileVO.AgeGroupAnalysis();
            analysis.setAgeGroupName(groupNames.get(groupKey));
            analysis.setTotalUsers(groupUsers.size());

            Set<Integer> userIds = groupUsers.stream()
                    .map(UserInfo::getId)
                    .collect(Collectors.toSet());

            List<UserQuestions> questions = userQuestionsService.list(Wrappers.<UserQuestions>lambdaQuery()
                    .in(UserQuestions::getUserId, userIds));

            int totalQuestions = questions.size();
            analysis.setTotalQuestions(totalQuestions);
            analysis.setAvgQuestionsPerUser(round(totalQuestions * 1.0 / groupUsers.size(), 2));

            BigDecimal totalIntegral = groupUsers.stream()
                    .map(UserInfo::getIntegral)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            analysis.setAvgIntegral(totalIntegral.divide(new BigDecimal(groupUsers.size()), 2, BigDecimal.ROUND_HALF_UP));

            long activeUsers = questions.stream()
                    .map(UserQuestions::getUserId)
                    .distinct()
                    .count();

            analysis.setActiveRate(round(activeUsers * 100.0 / groupUsers.size(), 2));

            analysis.setTopFocusAreas(extractTopFocusAreas(questions, 3));
            analysis.setPreferredTimeSlot(preferredTimeSlot(questions));

            analysisList.add(analysis);
        }

        return analysisList;
    }

    /**
     * 构建部门/支部维度分析
     */
    private List<UserProfileVO.DepartmentAnalysis> buildDepartmentAnalysis() {
        List<UserProfileVO.DepartmentAnalysis> deptAnalysisList = new ArrayList<>();

        List<StaffInfo> staffList = staffInfoService.list();
        if (staffList.isEmpty()) {
            return deptAnalysisList;
        }

        Map<Integer, List<StaffInfo>> deptMap = staffList.stream()
                .filter(staff -> staff.getDeptId() != null)
                .collect(Collectors.groupingBy(StaffInfo::getDeptId));

        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        Date monthStart1 = java.util.Date.from(firstDayOfMonth.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
        String monthStart = DateUtil.formatDateTime(monthStart1);
        String monthEnd = DateUtil.formatDateTime(new Date());

        int rank = 1;
        for (Map.Entry<Integer, List<StaffInfo>> entry : deptMap.entrySet()) {
            Integer deptId = entry.getKey();
            List<StaffInfo> deptStaff = entry.getValue();

            UserProfileVO.DepartmentAnalysis analysis = new UserProfileVO.DepartmentAnalysis();
            analysis.setDepartmentId(deptId);
            analysis.setDepartmentName(deptStaff.get(0).getDeptName());
            analysis.setDepartmentType("DEPARTMENT");
            analysis.setMemberCount(deptStaff.size());

            Set<Integer> userIds = deptStaff.stream()
                    .map(StaffInfo::getSysUserId)
                    .filter(Objects::nonNull)
                    .map(Integer::valueOf)
                    .collect(Collectors.toSet());

            int monthlyQuestions = userQuestionsService.count(Wrappers.<UserQuestions>lambdaQuery()
                    .in(UserQuestions::getUserId, userIds)
                    .ge(UserQuestions::getCreatedAt, monthStart)
                    .le(UserQuestions::getCreatedAt, monthEnd)) > 0 ?
                    (int) userQuestionsService.count(Wrappers.<UserQuestions>lambdaQuery()
                            .in(UserQuestions::getUserId, userIds)
                            .ge(UserQuestions::getCreatedAt, monthStart)
                            .le(UserQuestions::getCreatedAt, monthEnd)) : 0;

            analysis.setMonthlyQuestions(monthlyQuestions);
            analysis.setMonthlyAnswers(0);
            analysis.setResolutionRate(0.0);
            analysis.setAvgResponseTime(0.0);
            analysis.setActivityRank(rank++);

            deptAnalysisList.add(analysis);
        }

        return deptAnalysisList;
    }

    /**
     * 构建关注重点差异对比
     */
    private UserProfileVO.FocusComparison buildFocusComparison(Map<String, List<UserInfo>> ageGroupMap) {
        UserProfileVO.FocusComparison comparison = new UserProfileVO.FocusComparison();

        comparison.setYoungGroupFocus(extractFocusTopics(ageGroupMap.get("young")));
        comparison.setMiddleGroupFocus(extractFocusTopics(ageGroupMap.get("middle")));
        comparison.setSeniorGroupFocus(extractFocusTopics(ageGroupMap.get("senior")));

        return comparison;
    }

    /**
     * 提取关注话题
     */
    private List<UserProfileVO.FocusTopic> extractFocusTopics(List<UserInfo> groupUsers) {
        if (groupUsers == null || groupUsers.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Integer> userIds = groupUsers.stream()
                .map(UserInfo::getId)
                .collect(Collectors.toSet());

        List<UserQuestions> questions = userQuestionsService.list(Wrappers.<UserQuestions>lambdaQuery()
                .in(UserQuestions::getUserId, userIds));

        if (questions.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Integer> keywordCount = new HashMap<>();
        for (UserQuestions question : questions) {
            if (question.getKeyWord() != null && !question.getKeyWord().isEmpty()) {
                String[] keywords = question.getKeyWord().split(",");
                for (String keyword : keywords) {
                    keywordCount.put(keyword.trim(), keywordCount.getOrDefault(keyword.trim(), 0) + 1);
                }
            }
        }

        int totalQuestions = questions.size();

        return keywordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    UserProfileVO.FocusTopic topic = new UserProfileVO.FocusTopic();
                    topic.setKeyword(entry.getKey());
                    topic.setMentionCount(entry.getValue());
                    topic.setPercentage(round(entry.getValue() * 100.0 / totalQuestions, 2));
                    topic.setTrend(0);
                    return topic;
                })
                .collect(Collectors.toList());
    }

    /**
     * 提取主要关注领域
     */
    private List<String> extractTopFocusAreas(List<UserQuestions> questions, int limit) {
        if (questions.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Integer> keywordCount = new HashMap<>();
        for (UserQuestions question : questions) {
            if (question.getKeyWord() != null && !question.getKeyWord().isEmpty()) {
                String[] keywords = question.getKeyWord().split(",");
                for (String keyword : keywords) {
                    keywordCount.put(keyword.trim(), keywordCount.getOrDefault(keyword.trim(), 0) + 1);
                }
            }
        }

        return keywordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取活跃时段偏好
     */
    private String preferredTimeSlot(List<UserQuestions> questions) {
        if (questions.isEmpty()) {
            return "-";
        }

        Map<String, Integer> timeSlotCount = new HashMap<>();
        timeSlotCount.put("早晨", 0);
        timeSlotCount.put("上午", 0);
        timeSlotCount.put("中午", 0);
        timeSlotCount.put("下午", 0);
        timeSlotCount.put("傍晚", 0);
        timeSlotCount.put("晚间", 0);
        timeSlotCount.put("深夜", 0);

        for (UserQuestions question : questions) {
            try {
                java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(
                        question.getCreatedAt(),
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                int hour = createdAt.getHour();

                String timeSlot;
                if (hour >= 6 && hour < 9) {
                    timeSlot = "早晨";
                } else if (hour >= 9 && hour < 12) {
                    timeSlot = "上午";
                } else if (hour >= 12 && hour < 14) {
                    timeSlot = "中午";
                } else if (hour >= 14 && hour < 18) {
                    timeSlot = "下午";
                } else if (hour >= 18 && hour < 20) {
                    timeSlot = "傍晚";
                } else if (hour >= 20 && hour < 23) {
                    timeSlot = "晚间";
                } else {
                    timeSlot = "深夜";
                }

                timeSlotCount.put(timeSlot, timeSlotCount.getOrDefault(timeSlot, 0) + 1);
            } catch (Exception e) {
            }
        }

        return Collections.max(timeSlotCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    /**
     * 构建群体特征标签
     */
    private List<UserProfileVO.GroupFeatureTag> buildGroupFeatureTags(Map<String, List<UserInfo>> ageGroupMap) {
        List<UserProfileVO.GroupFeatureTag> featureTagsList = new ArrayList<>();

        Map<String, List<String>> tagMap = new LinkedHashMap<>();
        tagMap.put("young", Arrays.asList("学习积极性高", "关注时事热点", "善用 AI 工具", "提问频率高", "偏好晚间学习"));
        tagMap.put("middle", Arrays.asList("业务骨干", "关注政策解读", "注重实用性", "工作时间活跃", "问题解决率高"));
        tagMap.put("senior", Arrays.asList("经验丰富", "关注党建理论", "学习稳定", "偏好传统内容", "晨间学习为主"));

        Map<String, String> groupNames = new LinkedHashMap<>();
        groupNames.put("young", "年轻党员");
        groupNames.put("middle", "中年党员");
        groupNames.put("senior", "资深党员");

        Map<String, String> descriptions = new LinkedHashMap<>();
        descriptions.put("young", "35 岁以下年轻党员思维活跃，善于利用新技术，对党务工作充满热情，是学习的生力军。");
        descriptions.put("middle", "35-50 岁中年党员多为业务骨干，关注政策落地和实际问题解决，学习效果最为稳定。");
        descriptions.put("senior", "50 岁以上资深党员经验丰富，注重理论学习，学习习惯良好，起到传帮带作用。");

        for (Map.Entry<String, List<UserInfo>> entry : ageGroupMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                UserProfileVO.GroupFeatureTag featureTag = new UserProfileVO.GroupFeatureTag();
                featureTag.setGroupName(groupNames.get(entry.getKey()));
                featureTag.setFeatureTags(tagMap.get(entry.getKey()));
                featureTag.setGroupDescription(descriptions.get(entry.getKey()));
                featureTagsList.add(featureTag);
            }
        }

        return featureTagsList;
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