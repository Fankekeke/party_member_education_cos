package com.fank.f1k2.business.service;

import com.fank.f1k2.business.entity.vo.UserProfileVO;

/**
 * 用户画像统计服务接口
 *
 * @author FanK fan1ke2ke@gmail.com
 */
public interface IUserProfileService {

    /**
     * 获取用户画像统计数据
     *
     * @return 统计数据
     */
    UserProfileVO getUserProfile();
}