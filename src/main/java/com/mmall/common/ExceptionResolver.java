package com.mmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Slf4j
@Component //使用这个注解是为了把这个类注入到spring容器当中，如果不注入的话，这个类是没法进行全局异常处理的。
public class ExceptionResolver implements HandlerExceptionResolver{

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        log.error("{} Exception",httpServletRequest.getRequestURI(),e);
        //这里new MappingJacksonJsonView()的目的就是为了使返回给前端的是一个json格式数据
        ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView());

        //当使用是jackson2.x的时候使用MappingJackson2JsonView，课程中使用的是1.9。
        modelAndView.addObject("status",ResponseCode.ERROR.getCode());
        modelAndView.addObject("msg","接口异常,详情请查看服务端日志的异常信息");
        //下面这行代码也可以不写，不写的话，有异常的时候，返回给前端的就只会有状态和状态描述，而没有异常相关的信息
        modelAndView.addObject("data",e.toString());
        return modelAndView;
    }
}
