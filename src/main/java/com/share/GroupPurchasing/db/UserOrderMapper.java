package com.share.GroupPurchasing.db;

import com.share.GroupPurchasing.model.UserOrder;
import java.util.List;

public interface UserOrderMapper {

    int insertOrUpdateUserOrder(UserOrder userOrder);
    List<UserOrder> selectPage();
    List<UserOrder> selectByGroupsId(String groupsId);
    List<UserOrder> selectByOpenId(String openId);
    UserOrder selectByOutTradeNo(String outTradeNo);

}
