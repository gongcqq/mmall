package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import java.io.Serializable;

/**
 * 下面这个类是一个通用的用泛型构造的一个服务端响应对象
 */

//假设json文本中只有status字段有值，msg和data这两个字段虽然没值，但是还是会在json文本中显示，并且显示的
//值是null。使用下面这个注解的好处就是，当一个字段的值是null的时候，就不让这个字段再显示在json文本中啦。
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
    private int status;
    private String msg;
    private T data;

    private ServerResponse(int status){
        this.status=status;
    }

    private ServerResponse(int status,T data){
        this.status=status;
        this.data=data;
    }

    private ServerResponse(int status,String msg,T data){
        this.status=status;
        this.msg=msg;
        this.data=data;
    }

    private ServerResponse(int status,String msg){
        this.status=status;
        this.msg=msg;
    }

    //加上下面这个注解之后，当把ServerResponse类进行序列化返
    //回给前端时，下面这个方法就不会以字段形式显示在json中啦。
    @JsonIgnore
    public boolean isSuccess(){
        return this.status==ResponseCode.SUCCESS.getCode();
    }

    //下面这三个以get开头的方法在序列化的时候，都会显示在json中。
    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }


    public static <T>ServerResponse<T> createBySuccess(){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    public static <T>ServerResponse<T> createBySuccessMessage(String msg){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }

    //由于这个方法名和createBySuccessMessage方法的方法名不一样，所以就算最后data是String类型，到时候
    //只要调用下面这个方法，就不会把data的值传给msg，而是传给这个方法里的data。这就解决了当data的类型为
    //String类型时，由于与msg的类型冲突而无法接收到传入的值的问题。
    public static <T>ServerResponse<T> createBySuccess(T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }

    public static <T>ServerResponse<T> createBySuccess(String msg,T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }


    public static <T>ServerResponse<T> createByError(){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }

    public static <T>ServerResponse<T> createByErrorMessage(String errorMessage){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),errorMessage);
    }

    //以上静态方法中调用的枚举类的code的值不是0就是1，相当于code值变
    //成了一个常量，下面这个方法就是把code值作为一个变量来使用的方法。
    public static <T>ServerResponse<T> createByErrorCodeMessage(int errorCode,String errorMessage){
        return new ServerResponse<T>(errorCode,errorMessage);
    }
}



























