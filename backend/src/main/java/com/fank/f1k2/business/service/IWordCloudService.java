package com.fank.f1k2.business.service;

import com.fank.f1k2.business.entity.vo.WordCloudVO;

/**
 * 高频词云/话题排行统计服务接口
 *
 * @author FanK fan1ke2ke@gmail.com
 */
public interface IWordCloudService {

    /**
     * 获取高频词云统计数据
     *
     * @return 统计数据
     */
    WordCloudVO getWordCloudData();
}