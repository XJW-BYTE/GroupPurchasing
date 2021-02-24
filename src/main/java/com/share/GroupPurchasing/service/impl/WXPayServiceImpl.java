package com.share.GroupPurchasing.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.github.wxpay.sdk.WXPayUtil;
import com.share.GroupPurchasing.db.GroupsMapper;
import com.share.GroupPurchasing.db.UserMapper;
import com.share.GroupPurchasing.db.UserOrderMapper;
import com.share.GroupPurchasing.model.*;
import com.share.GroupPurchasing.service.WXPayService;
import com.share.GroupPurchasing.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class WXPayServiceImpl implements WXPayService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    GroupsMapper groupsMapper;

    @Autowired
    UserOrderMapper userOrderMapper;


    @Value("${weixinpay.appid}")
    private String appid;

    @Value("${weixinpay.mchId}")
    private String mchId;

    @Value("${weixinpay.storePassword}")
    private String storePassword;

    @Value("${weixinpay.unifiedorderUrl}")
    private String unifiedorderUrl;

    @Value("${weixinpay.refundorderUrl}")
    private String refundorderUrl;

    @Value("${weixinpay.notifyUrl}")
    private String notifyUrl;

    @Value("${member.price}")
    private float memberPrice;


    @Override
    public ResEntity wxPayGroups(HttpServletRequest request) throws Exception {

        request.setCharacterEncoding("UTF-8");
        ResEntity resEntity = new ResEntity();

        String openId = request.getParameter("openId");
        String sessionKey = request.getParameter("sessionKey");
        String groupsId = request.getParameter("groupsId");
        String goodsAmount = request.getParameter("goodsAmount");
        String body = request.getParameter("body");


        if(StringUtils.isBlank(openId) || StringUtils.isBlank(sessionKey)
                || StringUtils.isBlank(groupsId)){
            resEntity.setReturnCode(10002);
            resEntity.setErrMsg("参数非法 openId sessionKey groupsId goodsAmount body 不能为空");
            log.error("{参数非法 用户：["+openId+"] 创建订单失败 参数openId sessionKey groupsId goodsAmount body 不能为空}");
            return resEntity;
        }

        ResEntity authentication = AuthenticationUtil.authenticationSession(openId, sessionKey, userMapper);
        if(authentication.getReturnCode() != 200) {
            return authentication;
        }


        User user = userMapper.selectUserById(openId,"");
        if(user.getMemberEndDate().getTime() < DateUtil.getCurrentDt().getTime()) {
            resEntity.setReturnCode(10008);
            resEntity.setErrMsg("用户会员过期");
            log.info("{用户会员过期}");
            return resEntity;
        }


        Groups groups = groupsMapper.selectGroupsById(groupsId);
        if(groups.getIsValid() == 0 || groups.getGroupStatus() != GroupsStatusEnum.ONLINE.ordinal()) {
            resEntity.setReturnCode(10024);
            resEntity.setErrMsg("拼团不存在或已经结束");
            log.info("{拼团不存在或已经结束}");
            return resEntity;
        }

//        int joinUserAmount = groups.getJoinUserAmount();
//        int maxUserAmount = groups.getMaxUserAmount();
//        if((joinUserAmount + 1) > maxUserAmount ) {
//            resEntity.setReturnCode(10007);
//            resEntity.setErrMsg("购买数量超出拼团限制");
//            log.info("{购买数量超出拼团限制}");
//            return resEntity;
//        }

        //最终价格
        BigDecimal totalFee = groups.getGoodsPrice().multiply(BigDecimal.valueOf(Integer.valueOf(goodsAmount)));

        String out_trade_no = "groups"+UUID.randomUUID().toString().replace("-","").substring(0,13)+System.currentTimeMillis();

        Map<String, String> paraMap = new HashMap<String, String>();
        paraMap.put("appid", appid);
        paraMap.put("body", body);
        paraMap.put("mch_id", mchId);
        paraMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paraMap.put("openid", openId);
        paraMap.put("out_trade_no", out_trade_no);//订单号
        paraMap.put("spbill_create_ip", IpUtil.getIpAddr(request));
//        paraMap.put("total_fee",totalFee);
        paraMap.put("total_fee","1");
        paraMap.put("trade_type", "JSAPI");
        paraMap.put("notify_url",notifyUrl);
        String sign = WXPayUtil.generateSignature(paraMap, storePassword);
        paraMap.put("sign", sign);
        String xml = WXPayUtil.mapToXml(paraMap);
        String unifiedorder_url = unifiedorderUrl;

        log.info(xml);

        String xmlStr = HttpUtil.sendPost(unifiedorder_url, xml);
        Map<String, String> resMap = WXPayUtil.xmlToMap(xmlStr);

        if(! ("OK".equals(resMap.get("return_msg")) && "SUCCESS".equals(resMap.get("result_code")) && "SUCCESS".equals(resMap.get("return_code")))) {
            resEntity.setReturnCode(10043);
            resEntity.setErrMsg("用户 "+ openId +" 创建订单失败");
            log.error("用户 "+ openId +" 创建订单失败");
            log.error(JSONUtils.toJSONString(resMap));
            return resEntity;
        }

        UserOrder userOrder = new UserOrder();
        userOrder.setOutTradeNo(out_trade_no);
        userOrder.setOpenId(openId);
        userOrder.setGroupsId(Long.valueOf(groupsId));
        userOrder.setGoodsAmount(Integer.valueOf(goodsAmount));
        userOrder.setOrderPrice(totalFee);
        userOrder.setOrderVariety(OrderVarietyEnum.Groups.ordinal());
        userOrder.setOrderStatus(OrderStatusEnum.Created.ordinal());
        userOrder.setIsValid(1);

        int num = userOrderMapper.insertOrUpdateUserOrder(userOrder);
        if(num == 0) {
            log.error("用户 "+ openId +" 创建订单失败 持久化失败");
            resEntity.setReturnCode(10043);
            resEntity.setErrMsg("用户创建订单失败");
            log.error(resEntity.toString());
            return resEntity;
        }


        String prepay_id = (String)resMap.get("prepay_id");

        Map<String, String> payMap = new HashMap<String, String>();

        payMap.put("appId", appid);
        payMap.put("timeStamp", System.currentTimeMillis()+"");
        payMap.put("nonceStr", WXPayUtil.generateNonceStr());
        payMap.put("signType", "MD5");
        payMap.put("package", "prepay_id=" + prepay_id);
        log.info(WXPayUtil.mapToXml(payMap));
        String paySign = WXPayUtil.generateSignature(payMap, storePassword);

        Map<String, String> finalMap = new HashMap<String, String>();
        finalMap.put("appid",appid);
        finalMap.put("time_stamp",payMap.get("timeStamp"));
        finalMap.put("nonce_str",payMap.get("nonceStr"));
        finalMap.put("prepay_id",prepay_id);
        finalMap.put("sign",paySign);


        resEntity.setReturnCode(200);
        resEntity.setJsonStr(JSONUtils.toJSONString(finalMap));

        return resEntity;

    }

    @Override
    public ResEntity wxPayMember(HttpServletRequest request) throws Exception {

        request.setCharacterEncoding("UTF-8");
        ResEntity resEntity = new ResEntity();

        String openId = request.getParameter("openId");
        String sessionKey = request.getParameter("sessionKey");
        String body = request.getParameter("body");


        if(StringUtils.isBlank(openId) || StringUtils.isBlank(sessionKey)){
            resEntity.setReturnCode(10002);
            resEntity.setErrMsg("参数非法 openId sessionKey body 不能为空");
            log.error("{参数非法 用户：["+openId+"] 创建订单失败 参数openId sessionKey body 不能为空}");
            return resEntity;
        }

        ResEntity authentication = AuthenticationUtil.authenticationSession(openId, sessionKey, userMapper);
        if(authentication.getReturnCode() != 200) {
            return authentication;
        }

        float totalFee = memberPrice;

        String out_trade_no = "member"+UUID.randomUUID().toString().replace("-","").substring(0,13)+System.currentTimeMillis();

        Map<String, String> paraMap = new HashMap<String, String>();
        paraMap.put("appid", appid);
        paraMap.put("body", body);
        paraMap.put("mch_id", mchId);
        paraMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paraMap.put("openid", openId);
        paraMap.put("out_trade_no", out_trade_no);//订单号
        paraMap.put("spbill_create_ip", IpUtil.getIpAddr(request));
//        paraMap.put("total_fee",totalFee);
        paraMap.put("total_fee","1");
        paraMap.put("trade_type", "JSAPI");
        paraMap.put("notify_url",notifyUrl);
        String sign = WXPayUtil.generateSignature(paraMap, storePassword);
        paraMap.put("sign", sign);
        String xml = WXPayUtil.mapToXml(paraMap);
        String unifiedorder_url = unifiedorderUrl;

        log.info(xml);

        String xmlStr = HttpUtil.sendPost(unifiedorder_url, xml);
        Map<String, String> resMap = WXPayUtil.xmlToMap(xmlStr);

        if(! ("OK".equals(resMap.get("return_msg")) && "SUCCESS".equals(resMap.get("result_code")) && "SUCCESS".equals(resMap.get("return_code")))) {
            resEntity.setReturnCode(10043);
            resEntity.setErrMsg("用户 "+ openId +" 创建订单失败");
            log.error("用户 "+ openId +" 创建订单失败");
            log.error(JSONUtils.toJSONString(resMap));
            return resEntity;
        }


        UserOrder userOrder = new UserOrder();
        userOrder.setOutTradeNo(out_trade_no);
        userOrder.setOpenId(openId);
        userOrder.setOrderPrice(BigDecimal.valueOf(totalFee));
        userOrder.setOrderVariety(OrderVarietyEnum.Member.ordinal());
        userOrder.setOrderStatus(OrderStatusEnum.Created.ordinal());
        userOrder.setIsValid(1);

        int num = userOrderMapper.insertOrUpdateUserOrder(userOrder);
        if(num == 0) {
            log.error("用户 "+ openId +" 创建订单失败 持久化失败");
            resEntity.setReturnCode(10043);
            resEntity.setErrMsg("用户创建订单失败");
            log.error(resEntity.toString());
            return resEntity;
        }


        String prepay_id = (String)resMap.get("prepay_id");

        Map<String, String> payMap = new HashMap<String, String>();

        payMap.put("appId", appid);
        payMap.put("timeStamp", System.currentTimeMillis()+"");
        payMap.put("nonceStr", WXPayUtil.generateNonceStr());
        payMap.put("signType", "MD5");
        payMap.put("package", "prepay_id=" + prepay_id);
        log.info(WXPayUtil.mapToXml(payMap));
        String paySign = WXPayUtil.generateSignature(payMap, storePassword);

        Map<String, String> finalMap = new HashMap<String, String>();
        finalMap.put("appid",appid);
        finalMap.put("time_stamp",payMap.get("timeStamp"));
        finalMap.put("nonce_str",payMap.get("nonceStr"));
        finalMap.put("prepay_id",prepay_id);
        finalMap.put("sign",paySign);


        resEntity.setReturnCode(200);
        resEntity.setJsonStr(JSONUtils.toJSONString(finalMap));

        return resEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResEntity toPayCallBack(HttpServletRequest request, HttpServletResponse response) throws Exception {



        log.info("微信发送的callback信息-----------------");
        InputStream is = null;
        ResEntity resEntity = new ResEntity();

        request.setCharacterEncoding("UTF-8");

        is = request.getInputStream();//获取请求的流信息(这里是微信发的xml格式所有只能使用流来读)
        String xml = StreamUtil.inputStream2String(is);
        Map<String, String> notifyMap = WXPayUtil.xmlToMap(xml);//将微信发的xml转map

        if(notifyMap.get("out_trade_no").startsWith("groups")) {

            if(notifyMap.get("return_code").equals("SUCCESS")){
                if(notifyMap.get("result_code").equals("SUCCESS")){
                    String ordersSn = notifyMap.get("out_trade_no");
                    String transactionId = notifyMap.get("transaction_id");
                    String amountpaid = notifyMap.get("total_fee");//单位 分
                    BigDecimal amountPay = (new BigDecimal(amountpaid).divide(new BigDecimal("100"))).setScale(2);//将分转换成元-实际支付金额:元

                    log.info("微信支付成功，修改订单信息---------------");

                    UserOrder userOrder = userOrderMapper.selectByOutTradeNo(ordersSn);

                    if(userOrder == null) {
                        log.info("订单不存在--------------");
                        resEntity.setReturnCode(4001);
                        resEntity.setErrMsg("订单不存在");
                        return resEntity;
                    }

                    int order_status = userOrder.getOrderStatus();
                    String transaction_id = userOrder.getTransactionId();
                    if(OrderStatusEnum.PIED.ordinal() == order_status && transaction_id != null) {

                        log.info("订单已处理--------------");
                        resEntity.setReturnCode(200);
                        return resEntity;
                    } else {

                        log.info("userOrder.getOrder_price() "+userOrder.getOrderPrice());
                        log.info("amountPay "+amountPay);


//                if(userOrder.getOrder_price() != amountPay) {
//                    resEntity.setReturnCode(10045);
//                    resEntity.setErrMsg("订单回调处理失败,订单金额前后不一致");
//                    return resEntity;
//                }

                        Groups groups = groupsMapper.selectGroupsById(String.valueOf(userOrder.getGroupsId()));
                        groups.setJoinUserAmount(groups.getJoinUserAmount()+1);

                        userOrder.setTransactionId(transactionId);
                        userOrder.setOrderStatus(OrderStatusEnum.PIED.ordinal());

                        int numDbOrder = userOrderMapper.insertOrUpdateUserOrder(userOrder);
                        int numDbGroups = groupsMapper.insertOrUpdateGroups(groups);

                        if(numDbOrder == 0 || numDbGroups == 0) {
//                        resEntity.setReturnCode(10046);
//                        resEntity.setErrMsg("订单回调处理失败");
                            throw new Exception("订单回调处理失败");
//                        return resEntity;
                        }
                    }
                }

            }
        } else if(notifyMap.get("out_trade_no").startsWith("member")) {

            if(notifyMap.get("return_code").equals("SUCCESS")){
                if(notifyMap.get("result_code").equals("SUCCESS")){
                    String ordersSn = notifyMap.get("out_trade_no");
                    String transactionId = notifyMap.get("transaction_id");
                    String amountpaid = notifyMap.get("total_fee");//单位 分
                    BigDecimal amountPay = (new BigDecimal(amountpaid).divide(new BigDecimal("100"))).setScale(2);//将分转换成元-实际支付金额:元

                    log.info("微信支付成功，修改订单信息---------------");

                    UserOrder userOrder = userOrderMapper.selectByOutTradeNo(ordersSn);

                    if(userOrder == null) {
                        log.info("订单不存在--------------");
                        resEntity.setReturnCode(4001);
                        resEntity.setErrMsg("订单不存在");
                        return resEntity;
                    }

                    int order_status = userOrder.getOrderStatus();
                    String transaction_id = userOrder.getTransactionId();
                    if(OrderStatusEnum.PIED.ordinal() == order_status && transaction_id != null) {

                        log.info("订单已处理--------------");
                        resEntity.setReturnCode(200);
                        return resEntity;
                    } else {

                        log.info("userOrder.getOrder_price() "+userOrder.getOrderPrice());
                        log.info("amountPay "+amountPay);


//                if(userOrder.getOrder_price() != amountPay) {
//                    resEntity.setReturnCode(10045);
//                    resEntity.setErrMsg("订单回调处理失败,订单金额前后不一致");
//                    return resEntity;
//                }

                        User user = userMapper.selectUserById(userOrder.getOpenId(), "");
                        user.setMemberEndDate(DateUtil.addDays(user.getMemberEndDate(),30));

                        userOrder.setTransactionId(transactionId);
                        userOrder.setOrderStatus(OrderStatusEnum.PIED.ordinal());

                        int numDbOrder = userOrderMapper.insertOrUpdateUserOrder(userOrder);
                        int numDbUser = userMapper.insertOrUpdateUser(user);

                        if(numDbOrder == 0 || numDbUser == 0) {
//                        resEntity.setReturnCode(10046);
//                        resEntity.setErrMsg("订单回调处理失败");
                            throw new Exception("订单回调处理失败");
//                        return resEntity;
                        }
                    }
                }

            }
        }

        is.close();
        log.info("订单支付成功 ---------------");
        resEntity.setReturnCode(200);
        return resEntity;

    }

    @Override
    public ResEntity wxRefund(HttpServletRequest request) throws Exception {

        request.setCharacterEncoding("UTF-8");
        ResEntity resEntity = new ResEntity();

        String openId = request.getParameter("openId");
        String groupsId = request.getParameter("groupsId");
        String outTradeNo = request.getParameter("outTradeNo");
        String transactionId = request.getParameter("transactionId");

        if(StringUtils.isBlank(openId) || StringUtils.isBlank(transactionId)
                || StringUtils.isBlank(groupsId) || StringUtils.isBlank(outTradeNo)){
            resEntity.setReturnCode(10002);
            resEntity.setErrMsg("参数非法 openId transactionId  groupsId outTradeNo 不能为空");
            log.error("{参数非法 用户：["+openId+"] 退款失败 参数openId transactionId  groupsId outTradeNo不能为空}");
            return resEntity;
        }

        UserOrder userOrder = userOrderMapper.selectByOutTradeNo(outTradeNo);
        if(! (userOrder.getOrderStatus() == OrderStatusEnum.PIED.ordinal())) {
            resEntity.setReturnCode(10012);
            resEntity.setErrMsg("退款失败，订单未支付");
            return  resEntity;
        }


        String outRefundNo = "refund"+ UUID.randomUUID().toString().replace("-","").substring(0,13)+System.currentTimeMillis();

        Map<String, String> paraMap = new HashMap<String, String>();
        paraMap.put("appid", appid);
        paraMap.put("mch_id", mchId);
        paraMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paraMap.put("openid", openId);
        paraMap.put("transaction_id", transactionId);//微信支付订单号
        paraMap.put("total_fee", userOrder.getOrderPrice().toString());
        paraMap.put("refund_fee", userOrder.getOrderPrice().toString());
        paraMap.put("out_refund_no", outRefundNo);//商户退款单号
//        paraMap.put("trade_type", "JSAPI");
//        paraMap.put("notify_url",notifyUrl);
        String sign = WXPayUtil.generateSignature(paraMap, storePassword);
        paraMap.put("sign", sign);
        String xml = WXPayUtil.mapToXml(paraMap);
        String refundOrderUrl = refundorderUrl;


        String xmlStr = HttpUtil.sendPost(refundOrderUrl, xml);
        Map<String, String> resMap = WXPayUtil.xmlToMap(xmlStr);

        if(! ("OK".equals(resMap.get("return_msg")) && "SUCCESS".equals(resMap.get("result_code")) && "SUCCESS".equals(resMap.get("return_code")))) {    resEntity.setReturnCode(10043);
            resEntity.setErrMsg("用户 "+ openId +"订单 "+ outTradeNo +" 退款失败");
            log.error("用户 "+ openId +" 退款失败");
            log.error(JSONUtils.toJSONString(resMap));
            return resEntity;
        }

        userOrder.setOrderStatus(OrderStatusEnum.REFUND.ordinal());
        int num = userOrderMapper.insertOrUpdateUserOrder(userOrder);
        if(num == 0) {
            log.error("用户 "+ openId +" 订单 "+ outTradeNo +" 退款失败 持久化失败");
            resEntity.setReturnCode(10043);
            resEntity.setErrMsg("用户订单退款失败");
            log.error(resEntity.toString());
            return resEntity;
        }

        resEntity.setReturnCode(200);

        return resEntity;
    }
}
