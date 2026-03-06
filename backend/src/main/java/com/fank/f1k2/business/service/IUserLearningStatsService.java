package com.fank.f1k2.business.service;


import com.fank.f1k2.business.entity.vo.UserLearningStatsVO;

/**
 * 用户学习统计服务接口
 *
 * @author FanK fan1ke2ke@gmail.com
 */
public interface IUserLearningStatsService {

    /**
     * 获取用户个人学习档案统计数据
     *
     * @param userId 用户 ID
     * @return 统计数据
     */
    UserLearningStatsVO getUserLearningStats(Integer userId);
}