package com.fank.f1k2.business.service;

import com.fank.f1k2.business.entity.vo.AdminHomeStatsVO;

/**
 * 统计服务接口
 *
 * @author FanK fan1ke2ke@gmail.com
 */
public interface IAdminStatsService {

    /**
     * 获取后台管理首页统计数据
     *
     * @return 统计数据
     */
    AdminHomeStatsVO getHomeStats();
}
