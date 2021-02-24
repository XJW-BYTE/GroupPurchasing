package com.share.GroupPurchasing.controller;

import com.share.GroupPurchasing.model.ResEntity;
import com.share.GroupPurchasing.service.UserService;
import com.share.GroupPurchasing.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/user")
@Slf4j
@Validated
public class UserController {


    @Autowired
    UserService userService;


    @ResponseBody
    @PostMapping("/updateUser")
    String updateUser(@NotBlank(message = "openId 不能为空") String openId, @NotBlank(message = "sessionKey 不能为空") String sessionKey,
                      @NotBlank(message = "address 不能为空") String address){

        try{

            ResEntity resEntity = userService.updateUser(openId,sessionKey,address);
            return JSONUtil.bean2JsonStr(resEntity);
        }catch (Exception e) {

            ResEntity resEntity = new ResEntity();
            resEntity.setReturnCode(400);
            resEntity.setErrMsg("更新用户地址错误");
            String res = JSONUtil.bean2JsonStr(resEntity);
            log.error(e.toString());
            return res;
        }
    }


    @ResponseBody
    @PostMapping("/getUserInfo")
    String getUserInfo(@NotBlank(message = "openId 不能为空") String openId, @NotBlank(message = "sessionKey 不能为空") String sessionKey,
                       String phoneNumber){

        try{

            ResEntity resEntity = userService.getUserInfo(openId,phoneNumber,sessionKey);
            return JSONUtil.bean2JsonStr(resEntity);
        }catch (Exception e) {

            ResEntity resEntity = new ResEntity();
            resEntity.setReturnCode(400);
            resEntity.setErrMsg("获取用户info错误");
            String res = JSONUtil.bean2JsonStr(resEntity);
            log.error(e.toString());
            return res;
        }
    }


    @ResponseBody
    @PostMapping("/isBanUser")
    String isBanUser(HttpServletRequest request, @NotBlank(message = "openId 不能为空") String openId,
                     @NotBlank(message = "actionCode 不能为空") String actionCode) {

        try{

            ResEntity resEntity = userService.isBanUser(request,openId,actionCode);
            return JSONUtil.bean2JsonStr(resEntity);
        }catch (Exception e) {

            ResEntity resEntity = new ResEntity();
            resEntity.setReturnCode(400);
            resEntity.setErrMsg("ban用户错误");
            String res = JSONUtil.bean2JsonStr(resEntity);
            log.error(e.toString());
            return res;
        }
    }

    @ResponseBody
    @PostMapping("/getAllUser")
    String getAllUser(@NotBlank(message = "pageNum 不能为空") String pageNum, @NotBlank(message = "pageSize 不能为空") String pageSize) {

        try{

            ResEntity resEntity = userService.getAllUser(pageNum,pageSize);
            return JSONUtil.bean2JsonStr(resEntity);
        }catch (Exception e) {

            ResEntity resEntity = new ResEntity();
            resEntity.setReturnCode(400);
            resEntity.setErrMsg("查询所有用户错误");
            String res = JSONUtil.bean2JsonStr(resEntity);
            log.error(e.toString());
            return res;
        }

    }

}
