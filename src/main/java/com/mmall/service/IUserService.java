package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

public interface IUserService {
    //用户登陆
    ServerResponse<User> login(String username, String password);

    //用户注册
    ServerResponse<String> register(User user);

    //校验
    ServerResponse<String> checkValid(String str,String type);

    //用于找回密码的问题
    ServerResponse<String> selectQuestion(String username);

    //用于找回密码的问题的答案的校验
    ServerResponse<String> checkAnswer(String username,String question,String answer);

    //重置密码
    ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken);

    //登录状态下的重置密码
    ServerResponse<String> resetPassword(String passwordOld, String passwordNew,User user);

    //更新用户的个人信息
    ServerResponse<User> updateInformation(User user);

    //获取用户的个人信息
    ServerResponse<User> get_information(Integer userId);

    //校验是否是管理员
    ServerResponse checkAdminRole(User user);
}
































