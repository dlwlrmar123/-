package com.api.project.service.impl.inner;

import com.api.common.model.entity.UserInterfaceInfo;
import com.api.project.common.ErrorCode;
import com.api.project.exception.BusinessException;
import com.api.project.service.UserInterfaceInfoService;
import com.api.common.service.InnerUserInterfaceInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        return userInterfaceInfoService.invokeCount(interfaceInfoId, userId);
    }

    @Override
    public boolean invokeLeftNum(long interfaceInfoId, long userId) {
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //查询是否第一次调用接口
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("interfaceInfoId",interfaceInfoId);
        queryWrapper.eq("userId",userId);
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getOne(queryWrapper);
        if (userInterfaceInfo == null){
            //新用户分配次数 10次，后续可以将改方法设计在数据库默认值中，数据库默认插入值为0
            UserInterfaceInfo newUserInterfaceInfo = new UserInterfaceInfo();
            newUserInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
            newUserInterfaceInfo.setUserId(userId);
            newUserInterfaceInfo.setLeftNum(10);
            return userInterfaceInfoService.save(newUserInterfaceInfo);
        }
        //如果调用次数为0禁止调用
        Integer leftNum = userInterfaceInfo.getLeftNum();
        return leftNum > 0;
    }


}
