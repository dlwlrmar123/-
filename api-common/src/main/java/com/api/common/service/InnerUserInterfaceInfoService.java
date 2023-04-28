package com.api.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.api.common.model.entity.UserInterfaceInfo;
/**
 * 用户接口调用关系数据统计
 */
public interface InnerUserInterfaceInfoService {

    /**
     * 调用接口统计（调用接口后增加数据库中的调用次数）
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     *调用接口剩余次数
     */
    boolean invokeLeftNum(long interfaceInfoId, long userId);
}
