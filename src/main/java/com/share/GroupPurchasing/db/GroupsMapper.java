package com.share.GroupPurchasing.db;

import com.share.GroupPurchasing.model.Groups;

import java.util.List;

public interface GroupsMapper {

    int insertOrUpdateGroups(Groups user);

    List<Groups> selectPage();

    Groups selectGroupsById(String groupsId);

}
