package com.fank.f1k2.business.service;

import com.fank.f1k2.business.entity.vo.ActivityMetricsVO;

/**
 * 活跃度统计服务接口
 *
 * @author FanK fan1ke2ke@gmail.com
 */
public interface IActivityMetricsService {

    /**
     * 获取活跃度指标统计数据
     *
     * @return 统计数据
     */
    ActivityMetricsVO getActivityMetrics();
}