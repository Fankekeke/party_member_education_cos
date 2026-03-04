package com.fank.f1k2.business.service;

import com.fank.f1k2.business.entity.ChatRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author FanK
 */
public interface IChatRecordService extends IService<ChatRecord> {

    /**
     * 根据维修员ID获取沟通联系人列表
     *
     * @param staffId 维修员ID
     * @return 联系人列表
     */
    List<LinkedHashMap<String, Object>> getContactsByStaffId(Integer staffId);

    /**
     * 根据用户ID获取沟通联系人列表
     *
     * @param userId 用户ID
     * @return 联系人列表
     */
    List<LinkedHashMap<String, Object>> getContactsByUserId(Integer userId);
}
