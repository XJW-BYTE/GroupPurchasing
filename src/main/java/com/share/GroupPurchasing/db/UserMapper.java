package com.share.GroupPurchasing.db;

import com.share.GroupPurchasing.model.User;

import java.util.List;

public interface UserMapper {


    User selectUserById(String openId,String phoneNum);

    int insertOrUpdateUser(User user);

    User selectUserInfoAndOrdersAndGroups(String openId);

    List<User> selectPage();
}
