package com.share.GroupPurchasing.service;

import com.share.GroupPurchasing.model.ResEntity;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

public interface UserService {



    ResEntity wxVerify(HttpServletRequest request) throws UnsupportedEncodingException;

    ResEntity getUserInfo(String openId, String phoneNumber,String sessionKey);

    ResEntity isBanUser(HttpServletRequest request, String openId, String actionCode);

    ResEntity getAllUser(String pageNum, String pageSize);

    ResEntity updateUser(String openId, String sessionKey,String address);

}
