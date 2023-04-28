package com.api.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.api.common.model.entity.InterfaceInfo;
import com.api.common.model.entity.User;
import com.api.common.model.entity.UserInterfaceInfo;

public interface InnerInterfaceInfoService {

    /**
     * 从数据库中查询模拟接口是否存在（请求路径、请求方法、请求参数）
     */
    InterfaceInfo getInterfaceInfo(String path,String method);
}
