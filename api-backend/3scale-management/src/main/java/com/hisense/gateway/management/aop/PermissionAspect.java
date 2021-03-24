///*
// * Licensed Materials - Property of tenxcloud.com
// * (C) Copyright 2019 TenxCloud. All Rights Reserved.
// */
//
//package com.hisense.gateway.management.aop;
//
//import com.hisense.gateway.management.annotation.ControllerPermission;
//import com.hisense.gateway.library.exception.UnAuthorized;
//import com.hisense.gateway.management.service.permission.PermissionService;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//import java.lang.reflect.Method;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
///**
// * @author wangjunxia
// * @date 2019-09-06
// */
//@Slf4j
//@Aspect
//@Configuration
//public class PermissionAspect {
//
//    @Autowired
//    private PermissionService permissionService;
//
//    /**
//     * 获取注解中对方法的指定的 permission code
//     *
//     * @param joinPoint 切点
//     * @return permission code
//     * @throws Exception
//     */
//    public static String getControllerPermissionCode(JoinPoint joinPoint) throws ClassNotFoundException {
//        String targetName = joinPoint.getTarget().getClass().getName();
//        String methodName = joinPoint.getSignature().getName();
//        Object[] arguments = joinPoint.getArgs();
//        Class targetClass = Class.forName(targetName);
//        Method[] methods = targetClass.getMethods();
//        String permissionCode = "";
//        for (Method method : methods) {
//            if (method.getName().equals(methodName)) {
//                Class[] clazzs = method.getParameterTypes();
//                if (clazzs.length == arguments.length) {
//                    permissionCode = method.getAnnotation(ControllerPermission.class).permissionCode();
//                    break;
//                }
//            }
//        }
//        return permissionCode;
//    }
//
//    @Pointcut("@annotation(com.hisense.gateway.management.annotation.ControllerPermission)")
//    public void accessAspect() {
//    }
//
//    /**
//     * 前置通知 用于拦截用户的操作
//     *
//     * @param joinPoint 切点
//     */
//    @Before("accessAspect()")
//    public void doBefore(JoinPoint joinPoint) throws ClassNotFoundException {
//        //类名
//        String className = joinPoint.getTarget().getClass().getName();
//        //请求方法
//        String method = joinPoint.getSignature().getName() + "()";
//        //方法描述
//        String permissionCode = getControllerPermissionCode(joinPoint);
//        //访问控制
//        if (permissionService.access(permissionCode)) {
//            return;
//        }
//        printLog(className, method, permissionCode);
//        throw new UnAuthorized("Please confirm user and project roles!");
//    }
//
//    private void printLog(String className, String method, String permissionCode) {
//        HttpServletRequest request =
//                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        //设置日期格式
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        StringBuilder sb = new StringBuilder(1000);
//        sb.append("\n");
//        sb.append("*********************************Request请求***************************************");
//        sb.append("\n");
//        sb.append("ClassName     :  ").append(className).append("\n");
//        sb.append("RequestMethod :  ").append(method).append("\n");
//        sb.append("RequestType   :  ").append(request.getMethod()).append("\n");
//        sb.append("PermissionCode   :  ").append(permissionCode).append("\n");
//        sb.append("ServerAddr    :  ").append(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()).append("\n");
//        sb.append("**************************");
//        sb.append(df.format(new Date()));
//        sb.append("***********************************");
//        log.error(sb.toString());
//    }
//}
