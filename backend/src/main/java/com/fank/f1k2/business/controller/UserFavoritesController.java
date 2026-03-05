package com.fank.f1k2.business.controller;


import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fank.f1k2.business.entity.UserInfo;
import com.fank.f1k2.business.service.IUserInfoService;
import com.fank.f1k2.common.utils.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fank.f1k2.business.entity.UserFavorites;
import com.fank.f1k2.business.service.IUserFavoritesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

/**
 * 用户收藏 控制层
 *
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@RestController
@RequestMapping("/business/user-favorites")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserFavoritesController {

    private final IUserFavoritesService bulletinInfoService;

    private final IUserInfoService userInfoService;

    /**
     * 分页获取用户收藏
     *
     * @param page      分页对象
     * @param queryFrom 用户收藏
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<UserFavorites> page, UserFavorites queryFrom) {
        return R.ok(bulletinInfoService.queryPage(page, queryFrom));
    }

    /**
     * 查询用户收藏详情
     *
     * @param id 主键ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(bulletinInfoService.getById(id));
    }

    /**
     * 查询用户收藏列表
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(bulletinInfoService.list());
    }

    /**
     * 新增用户收藏
     *
     * @param addFrom 用户收藏对象
     * @return 结果
     */
    @PostMapping
    public R save(UserFavorites addFrom) {
        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, addFrom.getUserId()));
        addFrom.setUserId(userInfo.getId());
        addFrom.setCreatedAt(DateUtil.formatDateTime(new Date()));
        return R.ok(bulletinInfoService.save(addFrom));
    }

    /**
     * 修改用户收藏
     *
     * @param editFrom 用户收藏对象
     * @return 结果
     */
    @PutMapping
    public R edit(UserFavorites editFrom) {
        return R.ok(bulletinInfoService.updateById(editFrom));
    }

    /**
     * 删除用户收藏
     *
     * @param ids 删除的主键ID
     * @return 结果
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(bulletinInfoService.removeByIds(ids));
    }

}
