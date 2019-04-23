package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 用户登陆
     */
    @RequestMapping(value="login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse){
        ServerResponse<User> response = iUserService.login(username,password);
        if(response.isSuccess()){

            //这里是把session的id作为token传入到cookie中，作为cookie的value值，然后再把这个cookie响应给浏览器。
            CookieUtil.writeLoginToken(httpServletResponse,session.getId());

            //这里是把同一个session的id作为key值存入到redis中，然后把用户信息存入到该key值对应的value值中。
            RedisShardedPoolUtil.setEx(session.getId(), JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
            //当从浏览器中获取到所有cookie后，我们会先找到和当初存入cookie时的name值一样的那个cookie，然后取出这个cookie的value值，接着把这个value值
            //作为redis中键值对的key值在redis中进行查找，看redis中有没有这个key值对应的value值，如果有，那么这个value值就是用户的登录信息。

        }
        return response;
    }

    /**
     * 用户登出
     */
    @RequestMapping(value="logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse){
        //这个loginToken就是在用户登陆时我们作为value值存入cookie中的那个session的id
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        //删除浏览器中在用户登陆时我们手动存入的那个cookie信息
        CookieUtil.delLoginToken(httpServletRequest,httpServletResponse);
        //删除我们在登录时存入redis中的那个带有用户信息的键值对
        RedisShardedPoolUtil.del(loginToken);
        return ServerResponse.createBySuccess();
    }

    /**
     * 用户注册
     */
    @RequestMapping(value="register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){
        return iUserService.register(user);
    }

    /**
     * 校验
     * @param str 表示校验类型所对应的值，这个值是由用户输入的。
     * @param type 表示需要校验的类型，如username，email等。
     */
    @RequestMapping(value="check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type){
        return iUserService.checkValid(str,type);
    }

    /**
     * 获取用户登陆信息
     */
    @RequestMapping(value="get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest httpServletRequest){
        //这个loginToken就是在用户登陆时我们作为value值存入cookie中的那个session的id
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        //在redis中获取loginToken这个键对应的value值，即用户的登录信息
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        //将字符串形式的用户信息转换成user对象
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if(user != null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
    }

    /**
     * 用于找回密码的问题
     */
    @RequestMapping(value="forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }

    /**
     * 用于找回密码的问题的答案的校验
     */
    @RequestMapping(value="forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return iUserService.checkAnswer(username,question,answer);
    }

    /**
     * 重置密码
     */
    @RequestMapping(value="forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        return iUserService.forgetResetPassword(username,passwordNew,forgetToken);

    }

    /**
     * 登录状态下的重置密码
     */
    @RequestMapping(value="reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpServletRequest httpServletRequest,String passwordOld,String passwordNew){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);

        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    /**
     * 更新用户的个人信息
     */
    @RequestMapping(value="update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpServletRequest httpServletRequest,User user){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(userJsonStr,User.class);

        if(currentUser == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        //我的理解是，参数中传入的user中的属性的值都是用户输入的，而id属性又不会展示在
        //页面中，所以参数中传入的user是没有id属性值的。但是这里的currentUser却是数据
        //库中的用户数据，而用户数据一旦存入数据库，就会被自动加上id属性值。而现在这两个
        //用户又是同一个用户，所以这里可以直接把currentUser的id属性值赋给user。这样的
        //话，参数中传入的user就有了id属性值，而在sql的更新语句中是需要用到这个id的。
        //从另一方面说的话，这样也是为了防止越权问题，可以确保要更新数据的用户的id就是
        //当前已经登录的用户的id。
        user.setId(currentUser.getId());
        //用户名不更新，保持原样(下面这句代码，我觉得有没有都不影响，我主要是也实在没发现这句代码有什么用)
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if(response.isSuccess()){
            response.getData().setUsername(currentUser.getUsername());
            RedisShardedPoolUtil.setEx(loginToken, JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return response;
    }

    /**
     * 获取用户的个人信息(我们在更新用户信息的时候，要先获取才能进行更新)
     */
    @RequestMapping(value="get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpServletRequest httpServletRequest){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(userJsonStr,User.class);

        if(currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,需要强制登录status=10");
        }
        return iUserService.get_information(currentUser.getId());
    }
}


























