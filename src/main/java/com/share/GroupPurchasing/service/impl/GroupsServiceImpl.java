package com.share.GroupPurchasing.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.share.GroupPurchasing.db.GroupsMapper;
import com.share.GroupPurchasing.db.UserMapper;
import com.share.GroupPurchasing.db.UserOrderMapper;
import com.share.GroupPurchasing.model.*;
import com.share.GroupPurchasing.service.GroupsService;
import com.share.GroupPurchasing.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;


@Service
@Slf4j
public class GroupsServiceImpl implements GroupsService {


    @Value("${web.upload-path}")
    private String filePath;

    @Autowired
    GroupsMapper groupsMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserOrderMapper userOrderMapper;


    @Override
    public ResEntity uploadGroups(HttpServletRequest request) throws IOException {

        request.setCharacterEncoding("UTF-8");
        log.info("{开始新建拼团}");

        ResEntity resEntity = new ResEntity();

        String groupsName = request.getParameter("groupsName");
        String groupsTitle = request.getParameter("groupsTitle");
        String goodsName = request.getParameter("goodsName");
//        String goodsPhoto = request.getParameter("goodsPhoto");
        String goodsPrice = request.getParameter("goodsPrice");
        String goodsUnits = request.getParameter("goodsUnits");
        String goodsAmount = request.getParameter("goodsAmount");
        String joinUserAmount = request.getParameter("joinUserAmount");
        String targetUserAmount = request.getParameter("targetUserAmount");
        String minUserAmount = request.getParameter("minUserAmount");
        String maxUserAmount = request.getParameter("maxUserAmount");


        if(StringUtils.isBlank(groupsName) || StringUtils.isBlank(groupsTitle) || StringUtils.isBlank(goodsName) || StringUtils.isBlank(goodsPrice)
                || StringUtils.isBlank(goodsUnits) || StringUtils.isBlank(goodsAmount) || StringUtils.isBlank(joinUserAmount)
                || StringUtils.isBlank(targetUserAmount) || StringUtils.isBlank(minUserAmount) ||StringUtils.isBlank(maxUserAmount) ) {

            resEntity.setReturnCode(10002);
            resEntity.setErrMsg("新建拼团 参数非法");
            log.error("{新建拼团 参数非法}");
            return resEntity;
        }

        try {
            BigDecimal.valueOf(Double.valueOf(goodsPrice));
        }catch (Exception e) {
            resEntity.setReturnCode(10023);
            resEntity.setErrMsg("新建拼团失败 价格格式非法");
            log.error("{新建拼团失败 价格格式非法}");
            return resEntity;
        }

        String picPath = "";

        ResEntity groups_picture_res = UploadPicturesUtil.upload(request, filePath, "groups_picture",picPath);
        if(groups_picture_res.getReturnCode() != 200) {
            return groups_picture_res;
        }


        Groups groups = new Groups();
        groups.setGroupsName(groupsName);
        groups.setGoodsPhoto(picPath);
        groups.setGroupsTitle(groupsTitle);
        groups.setGroupsName(groupsName);
        groups.setGoodsPrice(BigDecimal.valueOf(Double.valueOf(goodsPrice)));
        groups.setGoodsUnits(goodsUnits);
        groups.setGoodsAmount(Integer.valueOf(goodsAmount));
        groups.setJoinUserAmount(Integer.valueOf(joinUserAmount));
        groups.setTargetUserAmount(Integer.valueOf(targetUserAmount));
        groups.setMinUserAmount(Integer.valueOf(targetUserAmount));
        groups.setMaxUserAmount(Integer.valueOf(maxUserAmount));
        groups.setGroupStatus(GroupsStatusEnum.ONLINE.ordinal());

        int num = groupsMapper.insertOrUpdateGroups(groups);

        if (num == 0) {
            resEntity.setReturnCode(10023);
            resEntity.setErrMsg("新建拼团失败");
            log.error("{新建拼团失败 插入数据库失败}");
            return resEntity;
        }

        log.info("{新建拼团成功}");
        resEntity.setReturnCode(200);
        resEntity.setJsonStr(JSONUtil.bean2JsonStr(groups));

        return resEntity;

    }


    @Override
    public ResEntity getAllGroupsPage(String openId,String sessionKey,String pageNum, String pageSize) {

        ResEntity resEntity = new ResEntity();

        ResEntity authentication = AuthenticationUtil.authenticationSession(openId, sessionKey, userMapper);

        if(authentication.getReturnCode() != 200) {
            return authentication;
        }

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(Integer.valueOf(pageNum));
        pageRequest.setPageSize(Integer.valueOf(pageSize));

        PageResult pageResult = PageUtil.getPageResult(pageRequest, getPageInfo(pageRequest));
        List<Groups> groupList = (List<Groups>) pageResult.getContent();

        if(groupList == null || groupList.size() == 0) {
            resEntity.setReturnCode(200);
            return resEntity;
        }

        resEntity.setReturnCode(200);
        resEntity.setJsonStr(JSONUtil.bean2JsonStr(pageResult));
        return resEntity;
    }

    @Override
    public ResEntity startGroups(String grupsId) {

        ResEntity resEntity = new ResEntity();

        Groups groups = groupsMapper.selectGroupsById(grupsId);
        groups.setGroupStatus(GroupsStatusEnum.RUN.ordinal());
        int num = groupsMapper.insertOrUpdateGroups(groups);

        if(num == 0) {
            log.error("开团失败 持久化失败");
            resEntity.setReturnCode(10011);
            resEntity.setErrMsg("开团失败");
            log.error(resEntity.toString());
            return resEntity;
        }

        resEntity.setReturnCode(200);
        return resEntity;
    }

    @Override
    public ResEntity dissolveGroups(String grupsId) throws Exception {

        ResEntity resEntity = new ResEntity();

        List<UserOrder> userOrderList = userOrderMapper.selectByGroupsId(grupsId);

        WxPayUtil wxPayUtil = new WxPayUtil();

        HashMap<Integer, String> refundFailMap = new HashMap<Integer, String>();

        for (UserOrder userOrder : userOrderList) {

            if( ! (userOrder.getOrderStatus() == OrderStatusEnum.PIED.ordinal())) {
                continue;
            }

            if( userOrder.getOrderStatus() == OrderStatusEnum.REFUND.ordinal()) {
                continue;
            }

            ResEntity resRefund = wxPayUtil.wxRefund(userOrder.getOpenId(), userOrder.getOutTradeNo(), userOrder.getTransactionId());

            if(resRefund.getReturnCode() != 200) {
                refundFailMap.put(resRefund.getReturnCode(),userOrder.getOutTradeNo());
            }

            Thread.sleep(200);

        }

        if(! refundFailMap.isEmpty()) {
            resEntity.setReturnCode(10013);
            resEntity.setErrMsg("解散团购失败,有订单退款失败");
        }

        resEntity.setReturnCode(200);

        return resEntity;
    }


    private PageInfo<Groups> getPageInfo(PageRequest pageRequest) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        List<Groups> users = groupsMapper.selectPage();
        return new PageInfo<Groups>(users);
    }

}
