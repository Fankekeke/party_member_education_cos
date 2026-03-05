package com.fank.f1k2.business.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fank.f1k2.business.entity.AnswerRecord;
import com.fank.f1k2.business.dao.AnswerRecordMapper;
import com.fank.f1k2.business.entity.CollectionInfo;
import com.fank.f1k2.business.entity.CollectionOptions;
import com.fank.f1k2.business.entity.UserInfo;
import com.fank.f1k2.business.entity.vo.CollectionItem;
import com.fank.f1k2.business.service.IAnswerRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fank.f1k2.business.service.IUserInfoService;
import com.fank.f1k2.common.exception.F1k2Exception;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author FanK fan1ke2ke@gmail.com（悲伤的橘子树）
 */
@Service
public class AnswerRecordServiceImpl extends ServiceImpl<AnswerRecordMapper, AnswerRecord> implements IAnswerRecordService {

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 分页获取答题记录
     *
     * @param page       分页对象
     * @param queryFrom 答题记录
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<AnswerRecord> page, AnswerRecord queryFrom) {
        return baseMapper.queryPage(page, queryFrom);
    }

    @Override
    public boolean addAnswerRecord(AnswerRecord answerRecord) throws F1k2Exception {
        if (CollectionUtil.isEmpty(answerRecord.getCollectionItemList())) {
            throw new F1k2Exception("请添加题目选项");
        }

        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, answerRecord.getUserId()));
        answerRecord.setUserId(userInfo.getId());
        answerRecord.setCreateDate(DateUtil.formatDateTime(new Date()));
        answerRecord.setAnswerDetail(JSONUtil.toJsonStr(answerRecord.getCollectionItemList()));
        return this.save(answerRecord);
    }
}
