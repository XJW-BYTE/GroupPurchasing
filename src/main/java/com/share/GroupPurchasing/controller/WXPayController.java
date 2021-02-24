package com.share.GroupPurchasing.controller;

import com.share.GroupPurchasing.model.ResEntity;
import com.share.GroupPurchasing.service.WXPayService;
import com.share.GroupPurchasing.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/WXPay")
@Slf4j
public class WXPayController {

    @Autowired
    WXPayService wxPayService;

    @ResponseBody
    @PostMapping("/toPayMember")
    public String wxPayMember(HttpServletRequest request) {

        try{

            ResEntity resEntity = wxPayService.wxPayMember(request);
            return JSONUtil.bean2JsonStr(resEntity);
        }catch (Exception e) {

            ResEntity resEntity = new ResEntity();
            resEntity.setReturnCode(400);
            resEntity.setErrMsg("用户支付Member错误");
            log.error("用户支付会员错误");
            String res = JSONUtil.bean2JsonStr(resEntity);
            log.error(e.toString());
            return res;
        }

    }


    @ResponseBody
    @PostMapping("/toPayGroups")
    public String wxPayGroups(HttpServletRequest request) {

        try{

            ResEntity resEntity = wxPayService.wxPayGroups(request);
            return JSONUtil.bean2JsonStr(resEntity);
        }catch (Exception e) {

            ResEntity resEntity = new ResEntity();
            resEntity.setReturnCode(400);
            resEntity.setErrMsg("用户支付Groups错误");
            log.error("用户支付拼团错误");
            String res = JSONUtil.bean2JsonStr(resEntity);
            log.error(e.toString());
            return res;
        }

    }


    @ResponseBody
    @PostMapping("/wxRefund")
    public String wxRefund(HttpServletRequest request) {

        try{

            ResEntity resEntity = wxPayService.wxRefund(request);
            return JSONUtil.bean2JsonStr(resEntity);
        }catch (Exception e) {

            ResEntity resEntity = new ResEntity();
            resEntity.setReturnCode(400);
            resEntity.setErrMsg("退款错误");
            log.error("退款错误");
            String res = JSONUtil.bean2JsonStr(resEntity);
            log.error(e.toString());
            return res;
        }

    }



    @RequestMapping("toPayCallBack")
    public String callBack(HttpServletRequest request, HttpServletResponse response){


        try{

            ResEntity resEntity = wxPayService.toPayCallBack(request,response);

            response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
            return null;
        }catch (Exception e) {

            ResEntity resEntity = new ResEntity();
            resEntity.setReturnCode(400);
            resEntity.setErrMsg("支付回调错误");
            log.error("支付回调错误");
            String res = JSONUtil.bean2JsonStr(resEntity);
            log.error(e.toString());
            return res;
        }
    }


}
