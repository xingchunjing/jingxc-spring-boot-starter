package top.jingxc.server.controller;

import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import top.jingxc.server.config.ConstantCommon;
import top.jingxc.server.config.ReturnResultError;
import top.jingxc.server.exception.ReturnCode200Exception;

@Log4j
public class BaseController {

    @ExceptionHandler(ReturnCode200Exception.class)
    public ReturnResultError exception200Handler(Exception e) {
        log.error("进入BaseController错误日志收集exception200Handler——————————————" + e.getMessage(), e);
        return ReturnResultError.builder().code(ConstantCommon.RETURN_CODE_999)
                .msg(StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : "error").data("自定义异常抛出").build();
    }

    @ExceptionHandler(Exception.class)
    public ReturnResultError exception(Exception e) {
        log.error("进入BaseController错误日志收集exception_handler——————————————" + e.getMessage(), e);
        return ReturnResultError.builder().code(ConstantCommon.RETURN_CODE_999)
                .msg(StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : "error").data("运行时异常抛出").build();
    }

}
