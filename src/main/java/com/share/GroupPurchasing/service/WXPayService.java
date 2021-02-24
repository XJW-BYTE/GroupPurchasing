package com.share.GroupPurchasing.service;

import com.share.GroupPurchasing.model.ResEntity;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WXPayService {

    ResEntity wxPayGroups(HttpServletRequest request) throws Exception;

    ResEntity wxPayMember(HttpServletRequest request) throws Exception;

    ResEntity wxRefund(HttpServletRequest request) throws Exception;

    ResEntity toPayCallBack(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
