package com.api.project.controller;

import com.api.common.model.entity.InterfaceInfo;
import com.api.project.service.InterfaceInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.api.project.annotation.AuthCheck;
import com.api.project.common.*;
import com.api.project.constant.CommonConstant;
import com.api.project.constant.UserConstant;
import com.api.project.exception.BusinessException;
import com.api.project.model.dto.userinterfaceinfo.UserInterfaceInfoAddRequest;
import com.api.project.model.dto.userinterfaceinfo.UserInterfaceInfoQueryRequest;
import com.api.project.model.dto.userinterfaceinfo.UserInterfaceInfoUpdateRequest;
import com.api.project.service.UserInterfaceInfoService;

import com.api.project.service.UserService;
import com.api.common.model.entity.User;
import com.api.common.model.entity.UserInterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 接口管理
 */
@RestController
@RequestMapping("/userInterfaceInfo")
@Slf4j
public class UserInterfaceInfoController {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private InterfaceInfoService interfaceInfoService;


    /**
     * 创建
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUserInterfaceInfo(@RequestBody UserInterfaceInfoAddRequest userInterfaceInfoAddRequest, HttpServletRequest request) {
        if (userInterfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoAddRequest, userInterfaceInfo);
        // 校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        userInterfaceInfo.setUserId(loginUser.getId());
        boolean result = userInterfaceInfoService.save(userInterfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newUserInterfaceInfoId = userInterfaceInfo.getId();
        return ResultUtils.success(newUserInterfaceInfoId);
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
        if (oldUserInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldUserInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = userInterfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserInterfaceInfo(@RequestBody UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        if (userInterfaceInfoUpdateRequest == null || userInterfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoUpdateRequest, userInterfaceInfo);
        // 参数校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, false);
        User user = userService.getLoginUser(request);
        long id = userInterfaceInfoUpdateRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
        if (oldUserInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldUserInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = userInterfaceInfoService.updateById(userInterfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 增加接口调用次数
     * 在接口原的次数上增加调用次数
     * @param userInterfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/updatetimes")
    public BaseResponse<Boolean> updateUserInterfaceInfoCount(@RequestBody UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest,
                                                              HttpServletRequest request) {
        if (userInterfaceInfoUpdateRequest == null || userInterfaceInfoUpdateRequest.getInterfaceInfoId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoUpdateRequest, userInterfaceInfo);
        // 参数校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, false);
        // 仅本人或管理员可修改
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = userInterfaceInfoService.addInvokeTimes(userInterfaceInfo.getInterfaceInfoId(), userInterfaceInfo.getUserId(),userInterfaceInfo.getLeftNum());
        return ResultUtils.success(result);
    }


    /**
     * 根据 id 获取
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserInterfaceInfo> getUserInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getById(id);
        return ResultUtils.success(userInterfaceInfo);
    }

    /**
     * 获取列表（仅管理员可使用）
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/list")
    public BaseResponse<List<UserInterfaceInfo>> listUserInterfaceInfo(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {
        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
        if (userInterfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfoQuery);
        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoService.list(queryWrapper);
        return ResultUtils.success(userInterfaceInfoList);
    }

//    /**
//     * 分页获取列表
//     */
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    @GetMapping("/list/page")
//    public BaseResponse<Page<UserInterfaceInfo>> listUserInterfaceInfoByPage(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest, HttpServletRequest request) {
//        if (userInterfaceInfoQueryRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
//        BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfoQuery);
//        long current = userInterfaceInfoQueryRequest.getCurrent();
//        long size = userInterfaceInfoQueryRequest.getPageSize();
//        String sortField = userInterfaceInfoQueryRequest.getSortField();
//        String sortOrder = userInterfaceInfoQueryRequest.getSortOrder();
//        // 限制爬虫
//        if (size > 50) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
//        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
//                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
//        Page<UserInterfaceInfo> userInterfaceInfoPage = userInterfaceInfoService.page(new Page<>(current, size), queryWrapper);
//        return ResultUtils.success(userInterfaceInfoPage);
//    }

    /**
     * 分页获取列表
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listUserInterfaceInfoByPage(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
        long current = 1;
        long size = 5;
//        // 限制爬虫
//        if (size > 10) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        queryWrapper.eq("userId", loginUser.getId()).orderByDesc("updateTime");
        Page<UserInterfaceInfo> userInterfaceInfoPage = userInterfaceInfoService.page(new Page<>(current, size), queryWrapper);
        List<UserInterfaceInfo> userInterfaceInfoList= userInterfaceInfoPage.getRecords();
        if (userInterfaceInfoList == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        List<InterfaceInfo> interfaceInfoList = new ArrayList<>();
        long totalCount = userInterfaceInfoList.stream().count();
        for (UserInterfaceInfo userInterfaceInfo : userInterfaceInfoList) {
            InterfaceInfo interfaceInfoVo = new InterfaceInfo();
            InterfaceInfo interfaceInfoById = interfaceInfoService.getById(userInterfaceInfo.getInterfaceInfoId());
            BeanUtils.copyProperties(interfaceInfoById, interfaceInfoVo);
            interfaceInfoList.add(interfaceInfoVo);
        }
        Page<InterfaceInfo> interfaceInfoPage = new Page<>();
        interfaceInfoPage.setRecords(interfaceInfoList);
        interfaceInfoPage.setTotal(totalCount);
        interfaceInfoPage.setCurrent(1);
        interfaceInfoPage.setSize(5);
        interfaceInfoPage.setOrders(userInterfaceInfoPage.orders());
        return ResultUtils.success(interfaceInfoPage);
    }


}
