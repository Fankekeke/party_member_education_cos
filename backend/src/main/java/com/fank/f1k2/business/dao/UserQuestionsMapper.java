package com.fank.f1k2.business.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fank.f1k2.business.entity.UserQuestions;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;

/**
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
public interface UserQuestionsMapper extends BaseMapper<UserQuestions> {

    /**
     * 分页获取AI答疑
     *
     * @param page      分页对象
     * @param queryFrom AI答疑
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryPage(Page<UserQuestions> page, @Param("queryFrom") UserQuestions queryFrom);
}
