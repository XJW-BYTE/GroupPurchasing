package com.share.GroupPurchasing.utils;

import com.share.GroupPurchasing.db.UserMapper;
import com.share.GroupPurchasing.model.ResEntity;
import com.share.GroupPurchasing.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class AuthenticationUtil {

    public static ResEntity authenticationSession(String openId, String sessionKey, UserMapper userMapper){

        ResEntity resEntity = new ResEntity();

//        boolean flag = false;

        User user = userMapper.selectUserById(openId,"");

        if(user == null || user.getIsValid() == 0) {
            resEntity.setReturnCode(10010);
            resEntity.setErrMsg("用户登陆过期/鉴权失败");
            log.info("{用户：[" + openId + "] 登陆过期/鉴权失败}");
            return resEntity;
        }

        if(StringUtils.isBlank(user.getSessionKey())) {
            resEntity.setReturnCode(10010);
            resEntity.setErrMsg("用户登陆过期/鉴权失败");
            log.info("{用户：[" + openId + "] 登陆过期/鉴权失败}");
            return resEntity;
        }

        if(! user.getSessionKey().equals(sessionKey)) {
            resEntity.setReturnCode(10010);
            resEntity.setErrMsg("用户登陆过期/鉴权失败");
            log.info("{用户：[" + openId + "] 登陆过期/鉴权失败}");
            return resEntity;
        }

        if(user.getExpiresIn() <= System.currentTimeMillis()) {
            resEntity.setReturnCode(10010);
            resEntity.setErrMsg("用户登陆过期/鉴权失败");
            log.info("{用户：[" + openId + "] 登陆过期/鉴权失败}");
            return resEntity;
        }

        if(user.getSessionKey().equals(sessionKey) && user.getExpiresIn() > System.currentTimeMillis()) {

            resEntity.setReturnCode(200);
        }

        return resEntity;
    }
}
