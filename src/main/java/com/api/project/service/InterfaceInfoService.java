package com.api.project.service;

import com.api.common.model.entity.User;
import com.api.project.model.vo.InterfaceInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.api.common.model.entity.InterfaceInfo;

/**
 *  接口信息服务
 */
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    /**
     *  创建接口
     * @param interfaceInfo
     * @param add
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    /**
     * 根据接口id返回接口封装信息（个人剩余调用次数）
     *
     * @param id
     * @param loginUser
     * @return
     */
    InterfaceInfoVO getInterfaceInfoById(long id, User loginUser);
}
