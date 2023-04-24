package com.api.project.controller;

import com.api.project.model.vo.InterfaceInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.api.project.annotation.AuthCheck;
import com.api.project.common.*;
import com.api.project.constant.CommonConstant;
import com.api.project.exception.BusinessException;
import com.api.project.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.api.project.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.api.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.api.project.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.api.project.model.enums.InterfaceInfoStatusEnum;
import com.api.project.service.InterfaceInfoService;
import com.api.project.service.UserService;
import com.api.sdk.client.ApiClient;
import com.api.common.model.entity.InterfaceInfo;
import com.api.common.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 接口管理
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private ApiClient apiClient;


    /**
     * 创建接口
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        // 如果空直接返回错误
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 把请求传递来的interfaceInfoAddRequest的参数拷贝到interfaceInfo
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // 校验参数合法性
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        // 创建人取当前登录的用户
        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.save(interfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        // 返回自增id
        return ResultUtils.success(interfaceInfo.getId());
    }

    /**
     * 删除(逻辑删除)
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 是否成功,返回布尔值
        return ResultUtils.success(interfaceInfoService.removeById(id));
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 校验参数合法性
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        User user = userService.getLoginUser(request);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 是否成功,返回布尔值
        return ResultUtils.success(interfaceInfoService.updateById(interfaceInfo));
    }

    /**
     * 根据id获取
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfoVO> getInterfaceInfoById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        return ResultUtils.success(interfaceInfoService.getById(id));
        //todo 对interfaceInfo创建一个带剩余调用次数的类
        User loginUser = userService.getLoginUser(request);
        InterfaceInfoVO interfaceInfoVO = interfaceInfoService.getInterfaceInfoById(id, loginUser);
        return ResultUtils.success(interfaceInfoVO);
    }

    /**
     * 获取列表（鉴权仅管理员可使用）
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        return ResultUtils.success(interfaceInfoService.list(queryWrapper));
    }

    /**
     * 分页获取列表
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        String description = interfaceInfoQuery.getDescription();
        // description 需支持模糊搜索
        interfaceInfoQuery.setDescription(null);
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // description 需支持模糊搜索
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(interfaceInfoPage);
    }


    /**
     * 发布
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // todo 上线前,应该通过InterfaceInfo访问一下接口,能访问得了再上线,这里只是做个简单的测试
        com.api.sdk.model.User user = new com.api.sdk.model.User();
        user.setUsername("test");
        String username = apiClient.getUserNameByPost(user);
        if (StringUtils.isBlank(username)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
        }
        // 仅本人或管理员可修改
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        return ResultUtils.success(interfaceInfoService.updateById(interfaceInfo));
    }

    /**
     * 下线
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        return ResultUtils.success(interfaceInfoService.updateById(interfaceInfo));
    }

    /**
     * 测试调用
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                    HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = interfaceInfoInvokeRequest.getId();
        String userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
//        String interfaceInfoName = oldInterfaceInfo.getName();
        // 判断是否下线
        if (oldInterfaceInfo.getStatus() == InterfaceInfoStatusEnum.OFFLINE.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口已下线");
        }
        // 拿自己的身份去操作
        User loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        ApiClient client = new ApiClient(accessKey, secretKey);

        String apiId = Long.toString(id);

        //筛选出调用方法
        Object result = "";
        // 把前端过来的userRequestParams转换成user.class
        Gson gson = new Gson();
        com.api.sdk.model.User user = gson.fromJson(userRequestParams, com.api.sdk.model.User.class);
        switch (apiId) {
            case "1":
                // 它就接入这个方法
                result = client.getUserNameByPost(user);
                break;
            case "2":
                result = client.getNameByPost(user);
                break;
            case "3":
                result = client.getNameByGet(user);
                break;
            default:
                result = "sorry,这是一个正在研发的接口";
        }
        return ResultUtils.success(result);
    }
}
//        Object result = reflectionInterface(ApiClient.class, interfaceInfoName, userRequestParams, accessKey, secretKey);
//        //网关拦截对异常处理
//        if (GateWayErrorCode.FORBIDDEN.getCode().equals(result)){
//            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR,"调用次数已用尽");
//        }
//        return ResultUtils.success(result);
//    }
//    public Object reflectionInterface(Class<?> reflectionClass, String methodName, String parameter, String accessKey, String secretKey) {
//        //构造反射类的实例
//        Object result = null;
//        try {
//            Constructor<?> constructor = reflectionClass.getDeclaredConstructor(String.class, String.class);
//            //获取SDK的实例，同时传入密钥
//            ApiClient apiClient = (ApiClient) constructor.newInstance(accessKey, secretKey);
//            //获取SDK中所有的方法
//            Method[] methods = apiClient.getClass().getMethods();
//            //筛选出调用方法
//            for (Method method : methods
//            ) {
//                if (method.getName().equals(methodName)) {
//                    //获取方法参数类型
//                    Class<?>[] parameterTypes = method.getParameterTypes();
//                    Method method1;
//                    if (parameterTypes.length == 0){
//                        method1 = apiClient.getClass().getMethod(methodName);
//                        return method1.invoke(apiClient);
//                    }
//                    method1 = apiClient.getClass().getMethod(methodName, parameterTypes[0]);
//                    //getMethod，多参会考虑重载情况获取方法,前端传来参数是JSON格式转换为String类型
//                    //参数Josn化
//                    Gson gson = new Gson();
//                    Object args = gson.fromJson(parameter, parameterTypes[0]);
//                    return result = method1.invoke(apiClient, args);
//                }
//            }
//        } catch (Exception e) {
//            log.error("反射调用参数错误",e);
//        }
//        return result;
//    }