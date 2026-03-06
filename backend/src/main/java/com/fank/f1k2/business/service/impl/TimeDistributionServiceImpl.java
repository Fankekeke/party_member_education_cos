package com.fank.f1k2.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fank.f1k2.business.entity.UserQuestions;
import com.fank.f1k2.business.entity.vo.TimeDistributionHeatmapVO;
import com.fank.f1k2.business.service.ITimeDistributionService;
import com.fank.f1k2.business.service.IUserQuestionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 时间分布热力图服务实现类
 *
 * @author FanK fan1ke2ke@gmail.com
 */
@Service
@RequiredArgsConstructor
public class TimeDistributionServiceImpl implements ITimeDistributionService {

    private final IUserQuestionsService userQuestionsService;

    private static final String[] DAY_NAMES = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    @Override
    public TimeDistributionHeatmapVO getTimeDistributionHeatmap() {
        TimeDistributionHeatmapVO heatmapVO = new TimeDistributionHeatmapVO();

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        String startDate = DateUtil.formatDateTime(DateUtil.parse(thirtyDaysAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        String endDate = DateUtil.formatDateTime(new Date());

        List<UserQuestions> questions = userQuestionsService.list(Wrappers.<UserQuestions>lambdaQuery()
                .ge(UserQuestions::getCreatedAt, startDate)
                .le(UserQuestions::getCreatedAt, endDate));

        if (questions.isEmpty()) {
            return buildEmptyHeatmap();
        }

        Map<Integer, Map<Integer, Integer>> dayHourMap = buildDayHourMap(questions);
        heatmapVO.setWeekData(buildWeekData(dayHourMap));
        heatmapVO.setBestTimeSuggestion(buildBestTimeSuggestion(dayHourMap));
        heatmapVO.setTimeSlotSummary(buildTimeSlotSummary(dayHourMap));

        return heatmapVO;
    }

    /**
     * 构建空的热力图数据
     */
    private TimeDistributionHeatmapVO buildEmptyHeatmap() {
        TimeDistributionHeatmapVO heatmapVO = new TimeDistributionHeatmapVO();

        List<TimeDistributionHeatmapVO.DayTimeSlot> weekData = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            TimeDistributionHeatmapVO.DayTimeSlot slot = new TimeDistributionHeatmapVO.DayTimeSlot()
                    .setDayOfWeek(i)
                    .setDayName(DAY_NAMES[i])
                    .setHourlyCounts(Arrays.asList(new Integer[24]))
                    .setTotalCount(0)
                    .setPeakHours("-");
            weekData.add(slot);
        }
        heatmapVO.setWeekData(weekData);

        return heatmapVO;
    }

    /**
     * 构建天 - 小时维度的数据统计
     */
    private Map<Integer, Map<Integer, Integer>> buildDayHourMap(List<UserQuestions> questions) {
        Map<Integer, Map<Integer, Integer>> dayHourMap = new HashMap<>();

        for (int i = 0; i < 7; i++) {
            Map<Integer, Integer> hourMap = new HashMap<>();
            for (int j = 0; j < 24; j++) {
                hourMap.put(j, 0);
            }
            dayHourMap.put(i, hourMap);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (UserQuestions question : questions) {
            try {
                LocalDateTime createdAt = LocalDateTime.parse(question.getCreatedAt(), formatter);
                int dayOfWeek = createdAt.getDayOfWeek().getValue() % 7;
                int hour = createdAt.getHour();

                Map<Integer, Integer> hourMap = dayHourMap.get(dayOfWeek);
                hourMap.put(hour, hourMap.getOrDefault(hour, 0) + 1);
            } catch (Exception e) {
                continue;
            }
        }

        return dayHourMap;
    }

    /**
     * 构建一周每天的时段数据
     */
    private List<TimeDistributionHeatmapVO.DayTimeSlot> buildWeekData(Map<Integer, Map<Integer, Integer>> dayHourMap) {
        List<TimeDistributionHeatmapVO.DayTimeSlot> weekData = new ArrayList<>();

        for (int day = 0; day < 7; day++) {
            Map<Integer, Integer> hourMap = dayHourMap.get(day);
            List<Integer> hourlyCounts = new ArrayList<>();

            int totalCount = 0;
            int maxCount = 0;
            int peakStart = 0;
            int peakEnd = 0;

            for (int hour = 0; hour < 24; hour++) {
                int count = hourMap.getOrDefault(hour, 0);
                hourlyCounts.add(count);
                totalCount += count;

                if (count > maxCount) {
                    maxCount = count;
                    peakStart = hour;
                    peakEnd = hour + 1;
                }
            }

            String peakHours = totalCount > 0 ? peakStart + ":00-" + peakEnd + ":00" : "-";

            TimeDistributionHeatmapVO.DayTimeSlot slot = new TimeDistributionHeatmapVO.DayTimeSlot()
                    .setDayOfWeek(day)
                    .setDayName(DAY_NAMES[day])
                    .setHourlyCounts(hourlyCounts)
                    .setTotalCount(totalCount)
                    .setPeakHours(peakHours);

            weekData.add(slot);
        }

        return weekData;
    }

    /**
     * 构建最佳推送时间建议
     */
    private TimeDistributionHeatmapVO.BestTimeSuggestion buildBestTimeSuggestion(Map<Integer, Map<Integer, Integer>> dayHourMap) {
        int maxQuestions = 0;
        int bestDay = 0;
        int bestHour = 0;

        for (Map.Entry<Integer, Map<Integer, Integer>> entry : dayHourMap.entrySet()) {
            int day = entry.getKey();
            Map<Integer, Integer> hourMap = entry.getValue();

            for (Map.Entry<Integer, Integer> hourEntry : hourMap.entrySet()) {
                int hour = hourEntry.getKey();
                int count = hourEntry.getValue();

                if (count > maxQuestions) {
                    maxQuestions = count;
                    bestDay = day;
                    bestHour = hour;
                }
            }
        }

        String bestTimeSlot = getTimeSlotName(bestHour);
        String specificTimeRange = (bestHour) + ":00-" + (bestHour + 1) + ":00";
        int recommendationLevel = calculateRecommendationLevel(maxQuestions, dayHourMap);

        return new TimeDistributionHeatmapVO.BestTimeSuggestion()
                .setBestDay(DAY_NAMES[bestDay])
                .setBestTimeSlot(bestTimeSlot)
                .setSpecificTimeRange(specificTimeRange)
                .setAvgQuestions(maxQuestions)
                .setRecommendationLevel(recommendationLevel);
    }

    /**
     * 获取时段名称
     */
    private String getTimeSlotName(int hour) {
        if (hour >= 6 && hour < 9) {
            return "早晨";
        } else if (hour >= 9 && hour < 12) {
            return "上午";
        } else if (hour >= 12 && hour < 14) {
            return "中午";
        } else if (hour >= 14 && hour < 18) {
            return "下午";
        } else if (hour >= 18 && hour < 20) {
            return "傍晚";
        } else if (hour >= 20 && hour < 23) {
            return "晚间";
        } else {
            return "深夜";
        }
    }

    /**
     * 计算推荐等级（1-5 星）
     */
    private int calculateRecommendationLevel(int maxQuestions, Map<Integer, Map<Integer, Integer>> dayHourMap) {
        int totalQuestions = 0;
        int count = 0;

        for (Map<Integer, Integer> hourMap : dayHourMap.values()) {
            for (int q : hourMap.values()) {
                totalQuestions += q;
                count++;
            }
        }

        double avgQuestions = count > 0 ? (double) totalQuestions / count : 0;

        if (maxQuestions >= avgQuestions * 3) {
            return 5;
        } else if (maxQuestions >= avgQuestions * 2.5) {
            return 4;
        } else if (maxQuestions >= avgQuestions * 2) {
            return 3;
        } else if (maxQuestions >= avgQuestions * 1.5) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * 构建时段汇总统计
     */
    private TimeDistributionHeatmapVO.TimeSlotSummary buildTimeSlotSummary(Map<Integer, Map<Integer, Integer>> dayHourMap) {
        TimeDistributionHeatmapVO.TimeSlotSummary summary = new TimeDistributionHeatmapVO.TimeSlotSummary();

        int morningCount = sumHours(dayHourMap, 6, 9);
        int forenoonCount = sumHours(dayHourMap, 9, 12);
        int noonCount = sumHours(dayHourMap, 12, 14);
        int afternoonCount = sumHours(dayHourMap, 14, 18);
        int eveningCount = sumHours(dayHourMap, 18, 20);
        int nightCount = sumHours(dayHourMap, 20, 23);
        int lateNightCount = sumHours(dayHourMap, 23, 24) + sumHours(dayHourMap, 0, 6);

        summary.setMorningCount(morningCount);
        summary.setForenoonCount(forenoonCount);
        summary.setNoonCount(noonCount);
        summary.setAfternoonCount(afternoonCount);
        summary.setEveningCount(eveningCount);
        summary.setNightCount(nightCount);
        summary.setLateNightCount(lateNightCount);

        Map<String, Integer> slotCounts = new LinkedHashMap<>();
        slotCounts.put("早晨", morningCount);
        slotCounts.put("上午", forenoonCount);
        slotCounts.put("中午", noonCount);
        slotCounts.put("下午", afternoonCount);
        slotCounts.put("傍晚", eveningCount);
        slotCounts.put("晚间", nightCount);
        slotCounts.put("深夜", lateNightCount);

        String mostActiveSlot = Collections.max(slotCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
        summary.setMostActiveTimeSlot(mostActiveSlot);

        return summary;
    }

    /**
     * 汇总指定小时范围的提问数
     */
    private int sumHours(Map<Integer, Map<Integer, Integer>> dayHourMap, int startHour, int endHour) {
        int sum = 0;
        for (Map<Integer, Integer> hourMap : dayHourMap.values()) {
            for (int hour = startHour; hour < endHour; hour++) {
                sum += hourMap.getOrDefault(hour, 0);
            }
        }
        return sum;
    }
}