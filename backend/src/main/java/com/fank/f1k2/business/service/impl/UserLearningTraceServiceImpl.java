package com.fank.f1k2.business.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fank.f1k2.business.entity.UserLearningTrace;
import com.fank.f1k2.business.dao.UserLearningTraceMapper;
import com.fank.f1k2.business.service.IUserLearningTraceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@Service
public class UserLearningTraceServiceImpl extends ServiceImpl<UserLearningTraceMapper, UserLearningTrace> implements IUserLearningTraceService {

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
}
