package com.share.GroupPurchasing.model;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class Groups {

    private long groupsId;

    private String groupsName;

    private String groupsTitle;

    private String goodsName;

    private String goodsPhoto;

    private BigDecimal goodsPrice;

    private String goodsUnits;

    private int goodsAmount;

    private int joinUserAmount;

    private int targetUserAmount;

    private int minUserAmount;

    private int maxUserAmount;

    private int groupStatus;

    private Timestamp createTime;

    private Timestamp updateTime;

    private int isValid;
}
