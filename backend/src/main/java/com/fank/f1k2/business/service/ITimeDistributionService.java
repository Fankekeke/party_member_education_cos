package com.fank.f1k2.business.service;

import com.fank.f1k2.business.entity.vo.TimeDistributionHeatmapVO;

/**
 * 时间分布热力图统计服务接口
 *
 * @author FanK fan1ke2ke@gmail.com
 */
public interface ITimeDistributionService {

    /**
     * 获取时间分布热力图统计数据
     *
     * @return 统计数据
     */
    TimeDistributionHeatmapVO getTimeDistributionHeatmap();
}