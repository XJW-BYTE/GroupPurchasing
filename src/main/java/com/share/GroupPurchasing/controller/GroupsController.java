package com.share.GroupPurchasing.controller;

import com.share.GroupPurchasing.model.ResEntity;
import com.share.GroupPurchasing.service.GroupsService;
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
@RequestMapping("/groups")
@Slf4j
@Validated
public class GroupsController {


    @Autowired
    GroupsService groupsService;


    @ResponseBody
    @PostMapping("/uploadGroups")
    String uploadGroups(HttpServletRequest request) {

        try {

            ResEntity resEntity = groupsService.uploadGroups(request);
            return JSONUtil.bean2JsonStr(resEntity);
        } catch (Exception e) {
            ResEntity resEntity = new ResEntity();
            resEntity.setReturnCode(400);
            resEntity.setErrMsg("用户添加拼团错误");
            String res = JSONUtil.bean2JsonStr(resEntity);
            log.error(e.toString());
            return res;
        }
    }


    @ResponseBody
    @PostMapping("/startGroups")
    String startGroups(@NotBlank(message = "grupsId 不能为空") String grupsId) {

        try {

            ResEntity resEntity = groupsService.startGroups(grupsId);
            return JSONUtil.bean2JsonStr(resEntity);
        } catch (Exception e) {
            ResEntity resEntity = new ResEntity();
            resEntity.setReturnCode(400);
            resEntity.setErrMsg("开团错误");
            String res = JSONUtil.bean2JsonStr(resEntity);
            log.error(e.toString());
            return res;
        }
    }


    @ResponseBody
    @PostMapping("/dissolveGroups")
    String dissolveGroups(@NotBlank(message = "grupsId 不能为空") String grupsId) {

        try {

            ResEntity resEntity = groupsService.dissolveGroups(grupsId);
            return JSONUtil.bean2JsonStr(resEntity);
        } catch (Exception e) {
            ResEntity resEntity = new ResEntity();
            resEntity.setReturnCode(400);
            resEntity.setErrMsg("解散拼团错误");
            String res = JSONUtil.bean2JsonStr(resEntity);
            log.error(e.toString());
            return res;
        }
    }



    @PostMapping(value="/getAllGroupsPage")
    @ResponseBody
    String getAllGroupsPage(@NotBlank(message = "openId 不能为空") String openId, @NotBlank(message = "sessionKey 不能为空") String sessionKey,
                       @NotBlank(message = "pageNum 不能为空") String pageNum, @NotBlank(message = "pageSize 不能为空") String pageSize) {

        try {
            ResEntity resEntity = groupsService.getAllGroupsPage(openId,sessionKey,pageNum,pageSize);
            return JSONUtil.bean2JsonStr(resEntity);
        } catch (Exception e) {
            ResEntity resEntity = new ResEntity();
            resEntity.setReturnCode(400);
            resEntity.setErrMsg("用户获取拼团分页数据错误");
            String res = JSONUtil.bean2JsonStr(resEntity);
            log.error(e.getMessage());
            return res;
        }
    }

}
