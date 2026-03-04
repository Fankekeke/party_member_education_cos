package com.fank.f1k2.business.controller;


import cn.hutool.core.date.DateUtil;
import com.fank.f1k2.common.utils.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fank.f1k2.business.entity.PartyAnswers;
import com.fank.f1k2.business.service.IPartyAnswersService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

/**
 * 问题回答表 控制层
 *
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@RestController
@RequestMapping("/business/party-answers")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PartyAnswersController {

    private final IPartyAnswersService bulletinInfoService;

    /**
     * 分页获取问题回答表
     *
     * @param page      分页对象
     * @param queryFrom 问题回答表
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<PartyAnswers> page, PartyAnswers queryFrom) {
        return R.ok(bulletinInfoService.queryPage(page, queryFrom));
    }

    /**
     * 查询问题回答表详情
     *
     * @param id 主键ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(bulletinInfoService.getById(id));
    }

    /**
     * 查询问题回答表列表
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(bulletinInfoService.list());
    }

    /**
     * 新增问题回答表
     *
     * @param addFrom 问题回答表对象
     * @return 结果
     */
    @PostMapping
    public R save(PartyAnswers addFrom) {
        return R.ok(bulletinInfoService.save(addFrom));
    }

    /**
     * 修改问题回答表
     *
     * @param editFrom 问题回答表对象
     * @return 结果
     */
    @PutMapping
    public R edit(PartyAnswers editFrom) {
        return R.ok(bulletinInfoService.updateById(editFrom));
    }

    /**
     * 删除问题回答表
     *
     * @param ids 删除的主键ID
     * @return 结果
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(bulletinInfoService.removeByIds(ids));
    }

}
