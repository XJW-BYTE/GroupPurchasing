package com.share.GroupPurchasing.controller;

import com.share.GroupPurchasing.db.UserMapper;
import com.share.GroupPurchasing.model.ResEntity;
import com.share.GroupPurchasing.service.UserService;
import com.share.GroupPurchasing.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/"})
@Slf4j
public class WxVerifyController {


    @Autowired
    UserService userService;

    @Autowired
    UserMapper userMapper;

    @RequestMapping({"wxverify/MP_verify_A95JHfEDeBvyEvOa.txt"})
    @ResponseBody
    private String returnConfigFile() {
        return "A95JHfEDeBvyEvOa";
    }


    @RequestMapping({"wxverify"})
    @ResponseBody
    String wxVerify(HttpServletRequest request) {

        try{

            ResEntity resEntity = userService.wxVerify(request);
            return JSONUtil.bean2JsonStr(resEntity);
        }catch (Exception e) {

            ResEntity resEntity = new ResEntity();
            resEntity.setReturnCode(400);
            resEntity.setErrMsg("用户微信登陆授权失败");
            String res = JSONUtil.bean2JsonStr(resEntity);
            log.error(e.toString());
            return res;
        }

    }

}
