package com.share.GroupPurchasing.model;

import lombok.Data;

import java.sql.Timestamp;
import java.util.ArrayList;

@Data
public class User {

    private String openId;

    private String phoneNumber;

    private String address;

    private String sessionKey;

    private Long expiresIn;

    private Timestamp memberEndDate;

    private String nickName;

    private String userPhoto;

    private int gender;

    private Timestamp createTime;

    private Timestamp updateTime;

    private int isValid;

    private ArrayList<UserOrder> orderList;
}
