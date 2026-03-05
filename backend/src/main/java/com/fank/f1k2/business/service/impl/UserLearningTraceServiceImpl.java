package com.fank.f1k2.business.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fank.f1k2.business.entity.UserInfo;
import com.fank.f1k2.business.entity.UserLearningTrace;
import com.fank.f1k2.business.dao.UserLearningTraceMapper;
import com.fank.f1k2.business.service.IUserInfoService;
import com.fank.f1k2.business.service.IUserLearningTraceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@Service
public class UserLearningTraceServiceImpl extends ServiceImpl<UserLearningTraceMapper, UserLearningTrace> implements IUserLearningTraceService {

    @Resource
    private IUserInfoService userInfoService;

    @Resource
    private Generation generation;

    /**
     * 分页获取学习足迹与行为记录
     *
     * @param page      分页对象
     * @param queryFrom 学习足迹与行为记录
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<UserLearningTrace> page, UserLearningTrace queryFrom) {
        return baseMapper.queryPage(page, queryFrom);
    }

    /**
     * 统计用户学习足迹与行为记录
     *
     * @param userId 用户ID
     * @return 统计结果
     */
    @Override
    public LinkedHashMap<String, Object> statisticsUserLearningTrace(Integer userId) {
        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, userId));
        // 返回数据
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>() {
            {
                put("history", Collections.emptyList());
                put("recommend", null);
            }
        };

        List<LinkedHashMap<String, Object>> history = baseMapper.queryUserLearningTraceHistory(userId);
        if (CollectionUtil.isEmpty(history)) {
            String recommendTitles = history.stream()
                    .map(item -> item.get("title") != null ? item.get("title").toString() : "")
                    .filter(title -> !title.isEmpty())
                    .collect(Collectors.joining(","));

            String aiRecommend = generateLearningRecommend(recommendTitles);

            result.put("history", history);
            result.put("recommend", StrUtil.isEmpty(aiRecommend) ? Collections.emptyList() : StrUtil.split(aiRecommend, ","));
        }
        return result;
    }

    /**
     * 生成学习推荐
     *
     * @param titles 历史学习记录标题
     * @return 为空则返回空
     */
    private String generateLearningRecommend(String titles) {
        String prompt = String.format(
                "你是一名党员教育学习助手，请根据用户的历史学习记录生成个性化的学习推荐。" +
                        "要求：1.推荐 7 个左右相关学习内容；2.只返回推荐内容，用逗号分隔；3.不要包含任何解释说明。" +
                        "\n用户历史学习记录：%s", titles);

        String response = queryContent(prompt);
        return response;
    }

    /**
     * 从AI响应中提取学习推荐
     *
     * @param aiResponse AI响应
     * @return 学习推荐
     */
    private String extractRecommendation(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return "暂无学习推荐";
        }

        int startIndex = aiResponse.indexOf("【学习推荐：");
        if (startIndex == -1) {
            startIndex = aiResponse.indexOf("[学习推荐：");
        }

        if (startIndex == -1) {
            return aiResponse.trim();
        }

        startIndex += "【学习推荐：".length();
        int endIndex = aiResponse.indexOf("】", startIndex);

        if (endIndex == -1) {
            endIndex = aiResponse.indexOf("]", startIndex);
        }

        if (endIndex == -1) {
            return aiResponse.substring(startIndex).trim();
        }

        String extracted = aiResponse.substring(startIndex, endIndex).trim();
        return extracted.isEmpty() ? "暂无学习推荐" : extracted;
    }

    /**
     * 查询内容
     *
     * @param key 关键词
     * @return 内容
     */
    public String queryContent(String key) {
        Message userMessage = Message.builder()
                .role(Role.USER.getValue())
                .content(key)
                .build();
        GenerationParam param = GenerationParam.builder()
                //指定用于对话的通义千问模型名
                .model("qwen-plus")
                .messages(Arrays.asList(userMessage))
                //
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                //生成过程中核采样方法概率阈值，例如，取值为0.8时，仅保留概率加起来大于等于0.8的最可能token的最小集合作为候选集。
                // 取值范围为（0,1.0)，取值越大，生成的随机性越高；取值越低，生成的确定性越高。
                .topP(0.8)
                //阿里云控制台DASHSCOPE获取的api-key
                .apiKey("sk-fkebb4821588054a66aa1951d7f239f77c")
                //启用互联网搜索，模型会将搜索结果作为文本生成过程中的参考信息，但模型会基于其内部逻辑“自行判断”是否使用互联网搜索结果。
                .enableSearch(true)
                .build();
        GenerationResult generationResult = null;
        try {
            generationResult = generation.call(param);
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }
        List<String> allContents = generationResult.getOutput().getChoices().stream()
                .map(choice -> choice.getMessage().getContent())
                .collect(Collectors.toList());

        return String.join("\n---\n", allContents);
    }
}
