package com.share.GroupPurchasing.service;

import com.share.GroupPurchasing.model.ResEntity;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface GroupsService {


    ResEntity uploadGroups(HttpServletRequest request) throws IOException;


    ResEntity getAllGroupsPage(String openId,String sessionKey,String pageNum, String pageSize);


    ResEntity startGroups(String grupsId);


    ResEntity dissolveGroups(String grupsId) throws Exception;


}
