package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String username);

    int checkEmail(String email);

    /**
     * 关于下面这个方法中的@Param注解我这里特别说明一下。在mybatis中，传递多个参数和传递单个参数是有很大区别的。
     * 之前在学习mybatis的时候，对于多个参数的传递，我使用的是Map集合的方式。就是先把这些参数都放到Map集合中，
     * 然后在xml文件中写sql语句的时候，select标签的parameterType属性值指向的就是这里的Map集合，而不再是某一
     * 个参数。而下面这种@Param注解的方式和使用Map集合的方式是一样的，都是解决传递多个参数的方式。使用注解的
     * 话，在sql语句中占位符中的内容要和@Param注解括号里的内容保持一致，我这里为了方便，就把@Param括号内的
     * 内容与selectLogin方法的参数保持一致了。假设第一个注解括号内的内容我给改成了"usernameA"的话，那么在
     * 写sql语句时，sql语句中占位符的内容就应该是"usernameA"，而不是"username"。
     *
     * 有一点需要说明的是，老师在dao层只使用了接口，并没有使用接口的实现类，所以这里要想传递多个参数，就只能
     * 使用@Param注解的方式了。
     */
    //登陆之后可以通过下面这个方法把登录信息返回给前端。
    User selectLogin(@Param("username") String username, @Param("password") String password);

    String selectQuestionByUsername(String username);

    int checkAnswer(@Param("username") String username,@Param("question") String question,@Param("answer") String answer);

    int updatePasswordByUsername(@Param("username") String username,@Param("passwordNew") String passwordNew);

    int checkPassword(@Param("password") String password,@Param("userId") Integer userId);

    int checkEmailByUserId(@Param("email") String email,@Param("userId") Integer userId);
}















































