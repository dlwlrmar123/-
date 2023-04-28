package com.api.common.service;

import com.api.common.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.api.common.model.entity.InterfaceInfo;
import com.api.common.model.entity.UserInterfaceInfo;


/**
 * 用户服务
 */
public interface InnerUserService {

    /**
     * 数据库中查是否已分配给用户秘钥（accessKey）
     */
    User getInvokeUser(String accessKey);
}
