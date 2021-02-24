package com.share.GroupPurchasing.model;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class UserOrder {

    private String outTradeNo;

    private String transactionId;

    private String openId;

    private long groupsId;

    private int goodsAmount;

    private BigDecimal orderPrice;

    private int orderVariety;

    private int orderStatus;

    private Timestamp createTime;

    private Timestamp updateTime;

    private int isValid;

    private Groups groups;
}
