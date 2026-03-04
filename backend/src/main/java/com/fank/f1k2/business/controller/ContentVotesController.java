package com.fank.f1k2.business.controller;


import cn.hutool.core.date.DateUtil;
import com.fank.f1k2.common.utils.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fank.f1k2.business.entity.ContentVotes;
import com.fank.f1k2.business.service.IContentVotesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

/**
 * 顶踩投票记录表 控制层
 *
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@RestController
@RequestMapping("/business/content-votes")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ContentVotesController {

    private final IContentVotesService bulletinInfoService;

    /**
     * 分页获取顶踩投票记录表
     *
     * @param page      分页对象
     * @param queryFrom 顶踩投票记录表
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<ContentVotes> page, ContentVotes queryFrom) {
        return R.ok(bulletinInfoService.queryPage(page, queryFrom));
    }

    /**
     * 查询顶踩投票记录表详情
     *
     * @param id 主键ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(bulletinInfoService.getById(id));
    }

    /**
     * 查询顶踩投票记录表列表
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(bulletinInfoService.list());
    }

    /**
     * 新增顶踩投票记录表
     *
     * @param addFrom 顶踩投票记录表对象
     * @return 结果
     */
    @PostMapping
    public R save(ContentVotes addFrom) {
        return R.ok(bulletinInfoService.save(addFrom));
    }

    /**
     * 修改顶踩投票记录表
     *
     * @param editFrom 顶踩投票记录表对象
     * @return 结果
     */
    @PutMapping
    public R edit(ContentVotes editFrom) {
        return R.ok(bulletinInfoService.updateById(editFrom));
    }

    /**
     * 删除顶踩投票记录表
     *
     * @param ids 删除的主键ID
     * @return 结果
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(bulletinInfoService.removeByIds(ids));
    }

}
