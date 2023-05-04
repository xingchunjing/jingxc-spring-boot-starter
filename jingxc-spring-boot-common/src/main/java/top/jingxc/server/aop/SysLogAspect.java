package top.jingxc.server.aop;

import com.alibaba.fastjson.JSON;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import lombok.extern.log4j.Log4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Log4j
@Aspect
@Component
public class SysLogAspect {

    @Pointcut("@annotation(top.jingxc.server.aop.OperationLogger)")
    public void controllerAspect() {

    }

    /**
     * 前置通知
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Before("controllerAspect()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {

        Map<String, Object> param = new HashMap<>();

        String classType = joinPoint.getTarget().getClass().getName();
        Class<?> clazz = Class.forName(classType);
        String clazzName = clazz.getName();
        param.put("clazzName", clazzName);
        String methodName = joinPoint.getSignature().getName();
        param.put("methodName", methodName);
        String[] paramNames = getFieldsName(this.getClass(), clazzName, methodName);
        Object[] args = joinPoint.getArgs();
        param.put("args", args);
        param.put("paramNames", paramNames);
        log.info("******进入方法*********\n" + JSON.toJSONString(param)
                + "\n***************************************************************************************");

        param = null;
    }

    /**
     * 后置通知 打印返回值日志
     *
     * @param ret 返回值
     * @throws Throwable 异常
     */
    @AfterReturning(returning = "ret", pointcut = "controllerAspect()")
    public void doAfterReturning(JoinPoint joinPoint, Object ret) throws Throwable {

        Map<String, Object> param = new HashMap<>();

        String classType = joinPoint.getTarget().getClass().getName();
        Class<?> clazz = Class.forName(classType);
        String clazzName = clazz.getName();
        param.put("clazzName", clazzName);
        String methodName = joinPoint.getSignature().getName();
        param.put("methodName", methodName);
        param.put("ret", ret);
        log.info("******返回方法*********\n" + JSON.toJSONString(param)
                + "\n***************************************************************************************");

        param = null;
    }

    /**
     * 得到方法参数的名称
     *
     * @param cls        类
     * @param clazzName  类名
     * @param methodName 方法名
     * @return 参数名数组
     * @throws NotFoundException 异常
     */
    private static String[] getFieldsName(Class<?> cls, String clazzName, String methodName) throws NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        ClassClassPath classPath = new ClassClassPath(cls);
        pool.insertClassPath(classPath);

        CtClass cc = pool.get(clazzName);
        CtMethod cm = cc.getDeclaredMethod(methodName);
        MethodInfo methodInfo = cm.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        String[] paramNames = new String[cm.getParameterTypes().length];
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
        for (int i = 0; i < paramNames.length; i++) {
            paramNames[i] = attr.variableName(i + pos); // paramNames即参数名
        }
        return paramNames;
    }
}
