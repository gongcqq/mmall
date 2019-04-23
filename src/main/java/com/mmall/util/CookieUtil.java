package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Slf4j
public class CookieUtil {

    private final static String COOKIE_DOMAIN = ".happymmall.com";
    private final static String COOKIE_NAME = "mmall_login_token";


    public static String readLoginToken(HttpServletRequest request){
        Cookie[] cks = request.getCookies();
        if(cks != null){
            for(Cookie ck : cks){
                log.info("read cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                if(StringUtils.equals(ck.getName(),COOKIE_NAME)){
                    log.info("return cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                    return ck.getValue();
                }
            }
        }
        return null;
    }

    //使用setDomain()方法和setPath()方法的区别和作用：

    //X:domain=".happymmall.com"
    //a:A.happymmall.com            cookie:domain=A.happymmall.com;path="/"
    //b:B.happymmall.com            cookie:domain=B.happymmall.com;path="/"
    //c:A.happymmall.com/test/cc    cookie:domain=A.happymmall.com;path="/test/cc"
    //d:A.happymmall.com/test/dd    cookie:domain=A.happymmall.com;path="/test/dd"
    //e:A.happymmall.com/test       cookie:domain=A.happymmall.com;path="/test"

    /**
     * 假设把X对应的".happymmall.com"作为一级域名，把a对应的"A.happymmall.com"以及b对应的"B.happymmall.com"都作为其二级域名的话。那么a和b都能
     * 拿到X这个一级域名的cookie信息，而且拿到的是同一个cookie。但是同为二级域名的a和b不能拿到对方的cookie信息。而设置了path后，像c、d、e就都能拿
     * 到a的cookie信息，但是却都拿不到b的cookie信息。另外，c和d也都能拿到e的cookie信息，但是c和d却拿不到对方的cookie信息。当然，a、b、c、d、e肯
     * 定都是可以拿到X的cookie信息的。
     *
     * 总结：
     *   1.不管是几级域名，只要是同级域名，那么它们之间就不能拿到对方的cookie信息；
     *   2.二级域名、三级域名都可以拿到一级域名的cookie信息，三级域名也可以拿到二
     *   级域名的cookie信息，如果还有四级域名、五级域名的话，以此类推。
     */



    public static void writeLoginToken(HttpServletResponse response,String token){
        Cookie ck = new Cookie(COOKIE_NAME,token);
        //这里设置Domain的值主要是想让不同名称但是后缀相同的网站能使用同一个cookie，这里的值设置成了".happymmall.com"，如果把这个作为一级域名，
        //而把像"user.happymmall.com"和"img.happymmall.com"这样的网站设置成它的二级域名的话，那么这两个二级域名就能拿到一级域名的cookie了。
        //由于都是从一级域名拿到的cookie，所以这两个域名拿到的cookie是同一个cookie。
        ck.setDomain(COOKIE_DOMAIN);
        ck.setPath("/");//代表设置在根目录
        ck.setHttpOnly(true);//这样设置之后，将无法通过脚本获取cookie信息，这么做的目的是为了防止cookie信息泄露，以保证cookie信息的安全
        //单位是秒。
        //如果这个maxage不设置的话，cookie就不会写入硬盘，而是写在内存。只在当前页面有效。
        ck.setMaxAge(60 * 60 * 24 * 365);
        log.info("write cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
        response.addCookie(ck);
    }


    public static void delLoginToken(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cks = request.getCookies();
        if(cks != null){
            for(Cookie ck : cks){
                if(StringUtils.equals(ck.getName(),COOKIE_NAME)){
                    ck.setDomain(COOKIE_DOMAIN);
                    ck.setPath("/");
                    ck.setMaxAge(0);//这里表示将cookie的有效期设置为零，代表删除此cookie。
                    log.info("del cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                    //前面已经使用ck.setMaxAge(0)方法把cookie的有效期设置成了零，这里把有效期为零的
                    //cookie返回给浏览器后，浏览器就会把这个cookie删除掉。
                    response.addCookie(ck);
                    //由于这里只处理登录的cookie，所以cookie只有一个，所以一旦cookie进入了这个if语句
                    //中被处理过了，也就没必要再进行for循环操作了，所以这里直接return。
                    return;
                }
            }
        }
    }

}


