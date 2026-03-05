package com.fank.f1k2.business.service.impl;

import cn.hutool.core.date.DateUtil;
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
import com.fank.f1k2.business.entity.UserQuestions;
import com.fank.f1k2.business.dao.UserQuestionsMapper;
import com.fank.f1k2.business.service.IUserInfoService;
import com.fank.f1k2.business.service.IUserLearningTraceService;
import com.fank.f1k2.business.service.IUserQuestionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserQuestionsServiceImpl extends ServiceImpl<UserQuestionsMapper, UserQuestions> implements IUserQuestionsService {

    private final IUserInfoService userInfoService;

    private final IUserLearningTraceService userLearningTraceService;

    @Resource
    private Generation generation;

    /**
     * 分页获取AI答疑
     *
     * @param page      分页对象
     * @param queryFrom AI答疑
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<UserQuestions> page, UserQuestions queryFrom) {
        return baseMapper.queryPage(page, queryFrom);
    }

    /**
     * 新增AI答疑
     *
     * @param userQuestions AI答疑对象
     * @return 添加结果
     */
    @Override
    public boolean addUserQuestions(UserQuestions userQuestions) {
        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, userQuestions.getUserId()));
        userQuestions.setUserId(userInfo.getId());
        userQuestions.setCreatedAt(DateUtil.formatDateTime(new Date()));
        userQuestions.setStatus("status");
        String key = "以下是用户提出的问题：" + userQuestions.getContent() + "， 给出相应的回答并且分析用户问题概述，问题概述回答模板：【问题概述：xxx】 500字以内";
        String contentMessage = queryContent(key);
        userQuestions.setAdminReply(contentMessage);
        String problemOverview = extractProblemOverview(contentMessage);
        userQuestions.setKeyWord(problemOverview);

        this.save(userQuestions);

        UserLearningTrace userLearningTrace = new UserLearningTrace();
        userLearningTrace.setUserId(userInfo.getId());
        userLearningTrace.setActionType("搜索");
        userLearningTrace.setTargetId(userQuestions.getId());
        userLearningTrace.setCreatedAt(DateUtil.formatDateTime(new Date()));
        return userLearningTraceService.save(userLearningTrace);
    }

    /**
     * 解析问题概述
     *
     * @param aiResponse 问题结果
     * @return 结果
     */
    private String extractProblemOverview(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return "";
        }

        int startIndex = aiResponse.indexOf("【问题概述：");
        if (startIndex == -1) {
            startIndex = aiResponse.indexOf("[问题概述：");
        }

        if (startIndex == -1) {
            return aiResponse;
        }

        startIndex += "【问题概述：".length();
        int endIndex = aiResponse.indexOf("】", startIndex);

        if (endIndex == -1) {
            endIndex = aiResponse.indexOf("]", startIndex);
        }

        if (endIndex == -1) {
            return aiResponse.substring(startIndex).trim();
        }

        return aiResponse.substring(startIndex, endIndex).trim();
    }

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
