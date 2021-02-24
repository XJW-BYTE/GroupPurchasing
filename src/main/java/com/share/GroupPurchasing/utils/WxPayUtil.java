package com.share.GroupPurchasing.utils;

import com.alibaba.druid.support.json.JSONUtils;
import com.github.wxpay.sdk.WXPayUtil;
import com.share.GroupPurchasing.db.UserOrderMapper;
import com.share.GroupPurchasing.model.OrderStatusEnum;
import com.share.GroupPurchasing.model.ResEntity;
import com.share.GroupPurchasing.model.UserOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class WxPayUtil {

    @Value("${weixinpay.appid}")
    private String appid;

    @Value("${weixinpay.mchId}")
    private String mchId;

    @Value("${weixinpay.storePassword}")
    private String storePassword;

    @Value("${weixinpay.refundorderUrl}")
    private String refundorderUrl;

    @Autowired
    UserOrderMapper userOrderMapper;

    public ResEntity wxRefund(String openId,String outTradeNo, String transactionId) throws Exception {

        ResEntity resEntity = new ResEntity();

        if(StringUtils.isBlank(openId) || StringUtils.isBlank(transactionId)
                || StringUtils.isBlank(outTradeNo)){
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
