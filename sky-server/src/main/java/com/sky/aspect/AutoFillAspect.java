package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充
 */
@Aspect // 这个是一个切面类
@Component // 也要交给spring管理
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点（对哪些类的哪些方法进行拦截）
     * 通知（增强的部分）
     */
    // com.sky.mapper下所有的类所有的方法，这个粒度太粗了（查询，删除等方法不需要拦截），还需要拦截加入自定义注解的方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {

    }

    /**
     * 前置通知，在执行INSERT或者UPDATE之前，需要为公共字段赋值
     */
    @Before("autoFillPointCut()") // 表示这是一个前置通知
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段的填充...");

        // 获取被拦截的方法上的数据库操作类型（OperationType.INSERT、OperationType.UPDATE）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 方法前面对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); // 获取方法上的注解对象
        OperationType operationType = autoFill.value();// 获得数据库操作类型
        // 获取到当前被拦截的方法参数，实体对象，比如：void update(Employee employee)要获得employee
        Object[] args = joinPoint.getArgs();// 获得所有参数（是一个数组），上面例子中就是employee
        if (args == null || args.length == 0) { // 防止出现空指针
            return; // 没有参数，直接return
        }
        Object entity = args[0]; // 我们约定好了，实体要放在第1个位置，所以从数组中取出，因为实体类型是不确定的，所以用Object接收
        // 准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        // 获取当前登录用户的id
        Long currentId = BaseContext.getCurrentId();
        // 根据当前不同的操作类型，为对应的属性通过反射赋值
        if (operationType == OperationType.INSERT) {
            // INSERT，为4个公共字段赋值
            try {
                // 这里是类中的方法，第一个参数为函数名，第二个参数为函数类型的字节码
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                // 通过反射为对象属性赋值，把他赋值到实体中
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (operationType == OperationType.UPDATE) {
            // UPDATE，为两个公共字段赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                // 通过反射为对象属性赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
