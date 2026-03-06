package com.fank.f1k2.business.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fank.f1k2.business.entity.UserQuestions;
import com.fank.f1k2.business.entity.vo.WordCloudVO;
import com.fank.f1k2.business.service.IUserQuestionsService;
import com.fank.f1k2.business.service.IWordCloudService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 高频词云/话题排行服务实现类
 *
 * @author FanK fan1ke2ke@gmail.com
 */
@Service
@RequiredArgsConstructor
public class WordCloudServiceImpl implements IWordCloudService {

    private final IUserQuestionsService userQuestionsService;

    private static final Set<String> THEORY_KEYWORDS = new HashSet<>(Arrays.asList(
            "理论", "思想", "精神", "主义", "理念", "观念", "认识", "学习", "教育", "党课"
    ));

    private static final Set<String> POLICY_KEYWORDS = new HashSet<>(Arrays.asList(
            "政策", "规定", "条例", "办法", "意见", "通知", "要求", "部署", "改革", "创新"
    ));

    private static final Set<String> PRACTICE_KEYWORDS = new HashSet<>(Arrays.asList(
            "工作", "实务", "操作", "流程", "规范", "标准", "实践", "应用", "执行", "落实"
    ));

    @Override
    public WordCloudVO getWordCloudData() {
        WordCloudVO wordCloudVO = new WordCloudVO();

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        String startDate = DateUtil.formatDateTime(DateUtil.parse(thirtyDaysAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        String endDate = DateUtil.formatDateTime(new Date());

        List<UserQuestions> questions = userQuestionsService.list(Wrappers.<UserQuestions>lambdaQuery()
                .ge(UserQuestions::getCreatedAt, startDate)
                .le(UserQuestions::getCreatedAt, endDate));

        if (questions.isEmpty()) {
            return buildEmptyWordCloud();
        }

        Map<String, KeywordStats> keywordStatsMap = extractKeywordStats(questions);

        wordCloudVO.setWordCloud(buildWordCloud(keywordStatsMap));
        wordCloudVO.setTopicRanking(buildTopicRanking(keywordStatsMap, questions));
        wordCloudVO.setCategoryAnalysis(buildCategoryAnalysis(keywordStatsMap, questions));
        wordCloudVO.setEmergingTopics(buildEmergingTopics(questions));

        return wordCloudVO;
    }

    /**
     * 构建空的词云数据
     */
    private WordCloudVO buildEmptyWordCloud() {
        return new WordCloudVO();
    }

    /**
     * 提取关键词统计信息
     */
    private Map<String, KeywordStats> extractKeywordStats(List<UserQuestions> questions) {
        Map<String, KeywordStats> keywordStatsMap = new HashMap<>();

        for (UserQuestions question : questions) {
            processText(question.getContent(), keywordStatsMap, question);

            if (question.getKeyWord() != null && !question.getKeyWord().isEmpty()) {
                String[] keywords = question.getKeyWord().split(",");
                for (String keyword : keywords) {
                    String trimmedKeyword = keyword.trim();
                    if (!trimmedKeyword.isEmpty()) {
                        keywordStatsMap.computeIfAbsent(trimmedKeyword, k -> new KeywordStats())
                                .increment(question.getUserId());
                    }
                }
            }
        }

        return keywordStatsMap;
    }

    /**
     * 处理文本内容，提取关键词
     */
    private void processText(String text, Map<String, KeywordStats> keywordStatsMap, UserQuestions question) {
        if (text == null || text.isEmpty()) {
            return;
        }

        Set<String> extractedKeywords = extractKeywordsFromText(text);
        for (String keyword : extractedKeywords) {
            keywordStatsMap.computeIfAbsent(keyword, k -> new KeywordStats())
                    .increment(question.getUserId());
        }
    }

    /**
     * 从文本中提取关键词（简单分词）
     */
    private Set<String> extractKeywordsFromText(String text) {
        Set<String> keywords = new HashSet<>();

        String[] words = text.split("[，。！？；、\\s]+");

        for (String word : words) {
            if (word.length() >= 2) {
                if (THEORY_KEYWORDS.contains(word)) {
                    keywords.add(word);
                } else if (POLICY_KEYWORDS.contains(word)) {
                    keywords.add(word);
                } else if (PRACTICE_KEYWORDS.contains(word)) {
                    keywords.add(word);
                }
            }
        }

        return keywords;
    }

    /**
     * 构建词云数据
     */
    private List<WordCloudVO.WordCloudItem> buildWordCloud(Map<String, KeywordStats> keywordStatsMap) {
        List<Map.Entry<String, KeywordStats>> sortedList = keywordStatsMap.entrySet().stream()
                .sorted(Map.Entry.<String, KeywordStats>comparingByValue(
                        Comparator.comparingInt(k -> k.frequency).reversed()))
                .limit(50)
                .collect(Collectors.toList());

        int maxFrequency = sortedList.stream()
                .mapToInt(entry -> entry.getValue().frequency)
                .max()
                .orElse(1);

        List<WordCloudVO.WordCloudItem> wordCloud = new ArrayList<>();
        for (Map.Entry<String, KeywordStats> entry : sortedList) {
            String keyword = entry.getKey();
            KeywordStats stats = entry.getValue();

            double weight = round(stats.frequency * 1.0 / maxFrequency * 10, 2);

            WordCloudVO.WordCloudItem item = new WordCloudVO.WordCloudItem()
                    .setWord(keyword)
                    .setFrequency(stats.frequency)
                    .setWeight(weight)
                    .setCategory(categorizeKeyword(keyword));

            wordCloud.add(item);
        }

        return wordCloud;
    }

    /**
     * 对话题进行分类
     */
    private String categorizeKeyword(String keyword) {
        for (String theory : THEORY_KEYWORDS) {
            if (keyword.contains(theory)) {
                return "理论知识";
            }
        }

        for (String policy : POLICY_KEYWORDS) {
            if (keyword.contains(policy)) {
                return "政策热点";
            }
        }

        for (String practice : PRACTICE_KEYWORDS) {
            if (keyword.contains(practice)) {
                return "工作实务";
            }
        }

        return "其他";
    }

    /**
     * 构建话题排行榜
     */
    private List<WordCloudVO.TopicRankItem> buildTopicRanking(
            Map<String, KeywordStats> keywordStatsMap,
            List<UserQuestions> questions) {

        List<Map.Entry<String, KeywordStats>> sortedList = keywordStatsMap.entrySet().stream()
                .sorted(Map.Entry.<String, KeywordStats>comparingByValue(
                        Comparator.comparingInt(k -> k.frequency).reversed()))
                .limit(10)
                .collect(Collectors.toList());

        List<WordCloudVO.TopicRankItem> ranking = new ArrayList<>();
        int rank = 1;

        for (Map.Entry<String, KeywordStats> entry : sortedList) {
            String keyword = entry.getKey();
            KeywordStats stats = entry.getValue();

            List<String> relatedQuestions = questions.stream()
                    .filter(q -> q.getContent().contains(keyword) ||
                            (q.getKeyWord() != null && q.getKeyWord().contains(keyword)))
                    .limit(3)
                    .map(UserQuestions::getTitleOrContent)
                    .collect(Collectors.toList());

            int hotScore = calculateHotScore(stats.frequency, stats.participantCount);

            WordCloudVO.TopicRankItem item = new WordCloudVO.TopicRankItem()
                    .setRank(rank++)
                    .setKeyword(keyword)
                    .setMentionCount(stats.frequency)
                    .setParticipantCount(stats.participantCount)
                    .setHotScore(hotScore)
                    .setTrend(0)
                    .setRelatedQuestions(relatedQuestions);

            ranking.add(item);
        }

        return ranking;
    }

    /**
     * 计算热度指数
     */
    private int calculateHotScore(int frequency, int participants) {
        return (int) Math.round(frequency * 0.6 + participants * 10 * 0.4);
    }

    /**
     * 构建分类统计
     */
    private WordCloudVO.CategoryAnalysis buildCategoryAnalysis(
            Map<String, KeywordStats> keywordStatsMap,
            List<UserQuestions> questions) {

        WordCloudVO.CategoryAnalysis analysis = new WordCloudVO.CategoryAnalysis();

        analysis.setTheoryTopics(filterAndSortByCategory(keywordStatsMap, "理论知识", questions));
        analysis.setPolicyTopics(filterAndSortByCategory(keywordStatsMap, "政策热点", questions));
        analysis.setPracticeTopics(filterAndSortByCategory(keywordStatsMap, "工作实务", questions));

        return analysis;
    }

    /**
     * 按类别筛选和排序
     */
    private List<WordCloudVO.CategoryItem> filterAndSortByCategory(
            Map<String, KeywordStats> keywordStatsMap,
            String category,
            List<UserQuestions> questions) {

        int totalQuestions = questions.size();

        return keywordStatsMap.entrySet().stream()
                .filter(entry -> categorizeKeyword(entry.getKey()).equals(category))
                .sorted(Map.Entry.<String, KeywordStats>comparingByValue(
                        Comparator.comparingInt(k -> k.frequency).reversed()))
                .limit(5)
                .map(entry -> {
                    WordCloudVO.CategoryItem item = new WordCloudVO.CategoryItem();
                    item.setTopic(entry.getKey());
                    item.setCount(entry.getValue().frequency);
                    item.setPercentage(round(entry.getValue().frequency * 100.0 / totalQuestions, 2));
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建新兴话题
     */
    private List<WordCloudVO.EmergingTopic> buildEmergingTopics(List<UserQuestions> questions) {
        Map<String, KeywordStats> last7DaysMap = new HashMap<>();
        Map<String, KeywordStats> last30DaysMap = new HashMap<>();

        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        String sevenDaysAgoStr = DateUtil.formatDateTime(DateUtil.parse(sevenDaysAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

        for (UserQuestions question : questions) {
            if (question.getCreatedAt().compareTo(sevenDaysAgoStr) >= 0) {
                extractAndCount(question, last7DaysMap);
            }
            extractAndCount(question, last30DaysMap);
        }

        List<WordCloudVO.EmergingTopic> emergingTopics = new ArrayList<>();

        for (Map.Entry<String, KeywordStats> entry : last7DaysMap.entrySet()) {
            String keyword = entry.getKey();
            KeywordStats last7DaysStats = entry.getValue();
            KeywordStats last30DaysStats = last30DaysMap.getOrDefault(keyword, new KeywordStats());

            int growthCount = last7DaysStats.frequency;
            double growthRate = last30DaysStats.frequency > 0
                    ? round((last7DaysStats.frequency - last30DaysStats.frequency) * 100.0 / last30DaysStats.frequency, 2)
                    : 100.0;

            if (growthRate > 50) {
                int potentialIndex = Math.min(10, (int) (growthRate / 10));

                WordCloudVO.EmergingTopic topic = new WordCloudVO.EmergingTopic()
                        .setKeyword(keyword)
                        .setGrowthCount(growthCount)
                        .setGrowthRate(growthRate)
                        .setPotentialIndex(potentialIndex);

                emergingTopics.add(topic);
            }
        }

        return emergingTopics.stream()
                .sorted(Comparator.comparingInt(WordCloudVO.EmergingTopic::getPotentialIndex).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 提取并计数
     */
    private void extractAndCount(UserQuestions question, Map<String, KeywordStats> keywordStatsMap) {
        if (question.getKeyWord() != null && !question.getKeyWord().isEmpty()) {
            String[] keywords = question.getKeyWord().split(",");
            for (String keyword : keywords) {
                String trimmedKeyword = keyword.trim();
                if (!trimmedKeyword.isEmpty()) {
                    keywordStatsMap.computeIfAbsent(trimmedKeyword, k -> new KeywordStats())
                            .increment(question.getUserId());
                }
            }
        }
    }

    /**
     * 保留指定小数位
     */
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        return Math.round(value * factor) / (double) factor;
    }

    /**
     * 关键词统计内部类
     */
    private static class KeywordStats {
        int frequency = 0;
        Set<Integer> participantIds = new HashSet<>();
        int participantCount = 0;

        void increment(Integer userId) {
            frequency++;
            if (userId != null && participantIds.add(userId)) {
                participantCount++;
            }
        }
    }
}