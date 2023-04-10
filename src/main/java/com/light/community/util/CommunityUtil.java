package com.light.community.util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
/**
 * @author light
 * @Description
 * @create 2023-03-25 12:24
 */
public class CommunityUtil {
    //提供两个方法
    //1.生成随机字符串（
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }
    //2.MD5加密（对密码加密
    //特：只能加密，不能解密
    //提高安全性：将密码后加几位随机字符串后在整体加密
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }else{
            //调用Spring的工具进行加密
            return DigestUtils.md5DigestAsHex(key.getBytes());
        }
    }


}
