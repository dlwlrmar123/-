package com.api.project.service.impl;

import com.api.common.model.entity.User;
import com.api.common.model.entity.UserInterfaceInfo;
import com.api.project.model.vo.InterfaceInfoVO;
import com.api.project.service.UserInterfaceInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.api.project.common.ErrorCode;
import com.api.project.exception.BusinessException;
import com.api.project.mapper.InterfaceInfoMapper;
import com.api.project.service.InterfaceInfoService;
import com.api.common.model.entity.InterfaceInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 接口服务
 * @author api
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService {

    @Resource
    UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();
        Long userId = interfaceInfo.getUserId();
        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            if (StringUtils.isAnyBlank(description)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
            }
            if (StringUtils.isAnyBlank(url)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
            }
            if (StringUtils.isAnyBlank(method)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
            }
            if (userId <= 0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }

    }

    @Override
    public InterfaceInfoVO getInterfaceInfoById(long id, User loginUser) {
        InterfaceInfo interfaceInfo = this.getById(id);
        if (interfaceInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"接口不存在");
        }
        InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
        BeanUtils.copyProperties(interfaceInfo,interfaceInfoVO);
        //查询该用户剩余调用接口次数
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("interfaceInfoId",id);
        queryWrapper.eq("userId",loginUser.getId());
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getOne(queryWrapper);
        if (userInterfaceInfo != null){
            interfaceInfoVO.setLeftNum(userInterfaceInfo.getLeftNum());
            interfaceInfoVO.setTotalNum(userInterfaceInfo.getTotalNum());
        }
        return interfaceInfoVO;
    }
    
}




