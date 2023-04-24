package com.api.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.api.common.model.entity.UserInterfaceInfo;

import java.util.List;


public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * 接口被调用次数增加
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 查询调用次数最高的五个接口
     * @param limit
     * @return
     */
    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);


    /**
     * 增加接口调用次数
     *
     * @param interfaceInfoId
     * @param userId
     * @param leftNum
     * @return
     */
    boolean addInvokeTimes(long interfaceInfoId, long userId, int leftNum);

}
