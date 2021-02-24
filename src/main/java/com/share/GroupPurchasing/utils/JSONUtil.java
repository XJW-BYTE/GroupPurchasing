package com.share.GroupPurchasing.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JSONUtil {

    public static String getStringByKey(String jsonStr,String key) {

        JSONObject obj= JSON.parseObject(jsonStr);

        if (obj.containsKey(key)) {
            return obj.get(key).toString();
        } else {

            return "";
        }

    }

    public static String bean2JsonStr(Object obj) {

        if (obj != null) {
            return JSONObject.toJSON(obj).toString();
        }else {
            return "";
        }

    }

}
