package com.fank.f1k2.business.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fank.f1k2.business.entity.UserFavorites;
import com.fank.f1k2.business.dao.UserFavoritesMapper;
import com.fank.f1k2.business.service.IUserFavoritesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@Service
public class UserFavoritesServiceImpl extends ServiceImpl<UserFavoritesMapper, UserFavorites> implements IUserFavoritesService {

    /**
     * 分页获取用户收藏
     *
     * @param page      分页对象
     * @param queryFrom 用户收藏
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<UserFavorites> page, UserFavorites queryFrom) {
        return baseMapper.queryPage(page, queryFrom);
    }
}
