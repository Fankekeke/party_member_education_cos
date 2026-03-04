package com.fank.f1k2.business.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fank.f1k2.business.entity.UserNotes;
import com.fank.f1k2.business.dao.UserNotesMapper;
import com.fank.f1k2.business.service.IUserNotesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@Service
public class UserNotesServiceImpl extends ServiceImpl<UserNotesMapper, UserNotes> implements IUserNotesService {

    /**
     * 分页获取用户笔记
     *
     * @param page      分页对象
     * @param queryFrom 用户笔记
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<UserNotes> page, UserNotes queryFrom) {
        return baseMapper.queryPage(page, queryFrom);
    }
}
