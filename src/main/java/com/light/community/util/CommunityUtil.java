package com.light.community.util;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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

    //将传入的数据封装为json对象，json对象转换为json字符串进行返回

    /**
     *
     * @param code 状态码
     * @param msg 消息提示
     * @param map 具体信息
     * @return json字符串
     */
    public static String getJsonString(int code, String msg, Map<String, Object> map){
        JSONObject json=new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if(map!=null){
            for(String key:map.keySet()){ //通过key遍历map
                json.put(key,map.get(key));
            }
        }

        return json.toJSONString();

    }
    //重载
    public static String getJsonString(int code, String msg){
        return getJsonString(code,msg,null);
    }

    public static String getJsonString(int code){
        return getJsonString(code,null,null);
    }

    public  static void main(String[] args) {
        Map<String,Object> map=new HashMap<>();
        map.put("name","zhangsan");
        map.put("age",20);
        System.out.println(getJsonString(0, "ok", map));
    }

}
