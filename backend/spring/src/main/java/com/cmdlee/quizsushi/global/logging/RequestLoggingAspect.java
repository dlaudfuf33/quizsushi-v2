package com.cmdlee.quizsushi.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RequestLoggingAspect {

    @Around("execution(* com.cmdlee.quizsushi..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethod = className + "." + methodName;
        long start = System.currentTimeMillis();

        log.debug("▶️ Start ➜ {}", fullMethod);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.debug(fullMethod + " Completed in -> ⏱️{} ms", duration);
            return result;
        } catch (Exception e) {
            log.error("Error -> {}  💥 {}", fullMethod, e.getMessage(), e);

            throw e;
        }
    }
}