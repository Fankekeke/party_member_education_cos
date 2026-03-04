package com.fank.f1k2.business.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fank.f1k2.business.entity.QuestionBank;
import com.fank.f1k2.business.dao.QuestionBankMapper;
import com.fank.f1k2.business.service.IQuestionBankService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@Service
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements IQuestionBankService {

    /**
     * 分页获取题库信息
     *
     * @param page      分页对象
     * @param queryFrom 题库信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<QuestionBank> page, QuestionBank queryFrom) {
        return baseMapper.queryPage(page, queryFrom);
    }
}
