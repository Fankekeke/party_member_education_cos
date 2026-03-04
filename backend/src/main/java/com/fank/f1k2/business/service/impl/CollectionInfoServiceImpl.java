package com.fank.f1k2.business.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fank.f1k2.business.entity.CollectionInfo;
import com.fank.f1k2.business.dao.CollectionInfoMapper;
import com.fank.f1k2.business.service.ICollectionInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@Service
public class CollectionInfoServiceImpl extends ServiceImpl<CollectionInfoMapper, CollectionInfo> implements ICollectionInfoService {

    /**
     * 分页获取题目表
     *
     * @param page       分页对象
     * @param queryFrom 题目表
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<CollectionInfo> page, CollectionInfo queryFrom) {
        return baseMapper.queryPage(page, queryFrom);
    }
}
