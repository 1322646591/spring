package com.cyq;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 标题
 *
 * @author chenyuqing
 * @email chenyq06@kaisagroup.com
 * @date 2021-01-20 14:29:26
 */
@Aspect
@Component
@Scope("singleton")
public class Aop {

    @Pointcut("execution(* com.cyq.*.*(..))")
    public void pointCut() {

    }

    @Before("pointCut()")
    public void before(JoinPoint joinPoint) {
        System.out.println("before  " + joinPoint.getSignature().getName());
    }

}
