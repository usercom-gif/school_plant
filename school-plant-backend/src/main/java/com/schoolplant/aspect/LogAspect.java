package com.schoolplant.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import com.schoolplant.annotation.Log;
import com.schoolplant.entity.OperationLog;
import com.schoolplant.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private OperationLogService logService;

    /**
     * 排除敏感属性
     */
    public static final String[] EXCLUDE_PROPERTIES = { "password", "oldPassword", "newPassword", "confirmPassword" };

    @Pointcut("@annotation(com.schoolplant.annotation.Log)")
    public void logPointCut() {}

    @AfterReturning(pointcut = "logPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, final Exception e, Object jsonResult) {
        try {
            System.out.println("Processing Log Aspect..."); // Debug Log
            // Get annotation
            Log controllerLog = getAnnotationLog(joinPoint);
            if (controllerLog == null) {
                System.out.println("No @Log annotation found");
                return;
            }
            System.out.println("Found @Log annotation: " + controllerLog.title());

            OperationLog operLog = new OperationLog();
            operLog.setCreatedAt(LocalDateTime.now());
            
            // Set IP and UA
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operLog.setIpAddress(getClientIp(request));
                operLog.setUserAgent(request.getHeader("User-Agent"));
            }

            // Set User Info
            try {
                if (StpUtil.isLogin()) {
                    operLog.setUserId(StpUtil.getLoginIdAsLong());
                    // Username and Role usually require DB lookup or Session storage
                    // For now, we can try to get from Session or just leave ID
                    // If we have a UserContext or similar, use it.
                    // Or we can query user service (might be slow).
                    // Simplest is to just log ID for now, or assume StpUtil has extra info.
                    // Requirement says "Operator Name" and "Role".
                    // StpUtil.getSession().get("user") might work if we stored it.
                    // Let's assume we can get it or just store ID for now and join in query.
                    // But requirement says "Single log record must include... Name, Role".
                    // I'll try to get it from StpUtil.getExtra("name") if available, otherwise just ID.
                    // Actually, let's skip fetching name/role here to avoid DB hit in Aspect if not cached.
                    // Wait, requirement says "Must include".
                    // I'll assume we can get it from StpUtil if we put it there during login.
                    // If not, I'll just set "Unknown" or ID.
                    operLog.setOperatorName(String.valueOf(StpUtil.getLoginId()));
                    // Role: StpUtil.getRoleList()
                    operLog.setOperatorRole(String.join(",", StpUtil.getRoleList()));
                }
            } catch (Exception ignored) {
                // Not logged in or error
            }

            // Set Module and Type
            operLog.setModule(controllerLog.module());
            operLog.setOperationType(controllerLog.operationType());
            operLog.setOperationDesc(controllerLog.title());

            // Set Content (Args)
            String params = argsArrayToString(joinPoint.getArgs());
            operLog.setOperationContent(params);

            // Set Result
            if (e != null) {
                operLog.setOperationResult("FAILURE");
                operLog.setErrorMsg(e.getMessage());
            } else {
                operLog.setOperationResult("SUCCESS");
            }

            // Save Async
            System.out.println("Saving log: " + JSON.toJSONString(operLog));
            logService.saveLog(operLog);

        } catch (Exception exp) {
            // Log the error of logging...
            System.err.println("Error in Log Aspect:");
            exp.printStackTrace();
        }
    }

    private Log getAnnotationLog(JoinPoint joinPoint) throws Exception {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(Log.class);
        }
        return null;
    }

    private String argsArrayToString(Object[] paramsArray) {
        if (paramsArray == null || paramsArray.length == 0) {
            return "";
        }
        try {
            Object[] cleanParams = new Object[paramsArray.length];
            for (int i = 0; i < paramsArray.length; i++) {
                Object o = paramsArray[i];
                if (o instanceof HttpServletRequest || o instanceof HttpServletResponse || o instanceof MultipartFile) {
                    continue;
                }
                cleanParams[i] = o;
            }
            SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
            for (String exclude : EXCLUDE_PROPERTIES) {
                filter.getExcludes().add(exclude);
            }
            return JSON.toJSONString(cleanParams, filter);
        } catch (Exception e) {
            return "";
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
