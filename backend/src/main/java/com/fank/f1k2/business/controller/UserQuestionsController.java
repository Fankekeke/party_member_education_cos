package com.fank.f1k2.business.controller;


import cn.hutool.core.date.DateUtil;
import com.fank.f1k2.common.utils.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fank.f1k2.business.entity.UserQuestions;
import com.fank.f1k2.business.service.IUserQuestionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

/**
 * AI答疑 控制层
 *
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@RestController
@RequestMapping("/business/user-questions")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserQuestionsController {

    private final IUserQuestionsService bulletinInfoService;

    /**
     * 分页获取AI答疑
     *
     * @param page      分页对象
     * @param queryFrom AI答疑
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<UserQuestions> page, UserQuestions queryFrom) {
        return R.ok(bulletinInfoService.queryPage(page, queryFrom));
    }

    /**
     * 查询AI答疑详情
     *
     * @param id 主键ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(bulletinInfoService.getById(id));
    }

    /**
     * 查询AI答疑列表
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(bulletinInfoService.list());
    }

    /**
     * 新增AI答疑
     *
     * @param addFrom AI答疑对象
     * @return 结果
     */
    @PostMapping
    public R save(UserQuestions addFrom) {
        return R.ok(bulletinInfoService.save(addFrom));
    }

    /**
     * 修改AI答疑
     *
     * @param editFrom AI答疑对象
     * @return 结果
     */
    @PutMapping
    public R edit(UserQuestions editFrom) {
        return R.ok(bulletinInfoService.updateById(editFrom));
    }

    /**
     * 删除AI答疑
     *
     * @param ids 删除的主键ID
     * @return 结果
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(bulletinInfoService.removeByIds(ids));
    }

}
