package com.mmall.controller.common;

import com.mmall.common.Const;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang.StringUtils;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


public class SessionExpireFilter implements Filter {

    /**
     * 这里说一下使用这个过滤器的作用。
     * 由于session的有效期是30分钟，所以这就导致一个结果，那就是即便实现了session共享，但是当用户登陆30分钟后，登陆信息就会失效，就
     * 又要重新登录了，这样用户体验就很不好了。而使用下面这个过滤器的作用就是，在用户登陆后，只要用户和网站进行交互，就会把session的
     * 过期时间重置成30分钟。这里说的交互分很多种，比如用户查看了商品详情，比如用户把商品加入购物车等等。至于为什么非要进行下重置，我
     * 再举个例子。假设用户这时候登录了，这时候session还有30分钟就会过期。假设用户登陆后过了5分钟才与网站进行交互，比如查看商品详情
     * 什么的。如果不重置session的过期时间，那么再过25分钟，这个用户的登录信息就会失效，就要重新登录了。但是如果重置了，那么session
     * 的有效期就又会变成30分钟，也就是说，这时候session是30分钟后失效，而不是25分钟。如果用户再与网站进行交互，就又会重置session
     * 的过期时间。这样一来，只要用户在30分钟内至少和网站交互一次，那么该用户的登录信息就永不会失效。
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        //判断logintoken是否为空或者""，如果不为空的话，继续拿user信息
        if(StringUtils.isNotEmpty(loginToken)){
            //从redis中获取到字符串形式的用户信息
            String userJsonStr = RedisShardedPoolUtil.get(loginToken);
            //把获取到的用户信息转成user对象
            User user = JsonUtil.string2Obj(userJsonStr,User.class);
            if(user != null){
                //如果user不为空，则调用expire方法重置session的过期时间
                RedisShardedPoolUtil.expire(loginToken,Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
            }
        }
        filterChain.doFilter(servletRequest,servletResponse);//放行
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}

