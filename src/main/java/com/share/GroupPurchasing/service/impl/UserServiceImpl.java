package com.share.GroupPurchasing.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.share.GroupPurchasing.db.GroupsMapper;
import com.share.GroupPurchasing.db.UserMapper;
import com.share.GroupPurchasing.model.*;
import com.share.GroupPurchasing.service.UserService;
import com.share.GroupPurchasing.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.List;



@Service
@Slf4j
public class UserServiceImpl implements UserService {


    @Autowired
    UserMapper userMapper;

    @Autowired
    GroupsMapper groupsMapper;


    @Value("${wx.url}")
    private String wxLoginUrl;

    @Value("${wx.appid}")
    private String wxAppId;

    @Value("${wx.secret}")
    private String secret;

    @Value("${wx.access_token_url}")
    private String wx_access_token_url;

    @Value("${wx.official_accounts_appid}")
    private String wx_official_accounts_appid;

    @Value("${wx.official_accounts_secret}")
    private String wx_official_accounts_secret;

    @Value("${wx.access_token_state}")
    private String wx_access_token_state;

    @Value("${wx.userinfo_url}")
    private String wx_userinfo_url;


    @Override
    public ResEntity wxVerify(HttpServletRequest request) throws UnsupportedEncodingException {

        request.setCharacterEncoding("UTF-8");
        String code=request.getParameter("code");
        String state=request.getParameter("state");

        ResEntity resEntity = new ResEntity();

        if(StringUtils.isBlank(code)) {
            resEntity.setReturnCode(10002);
            resEntity.setErrMsg("参数非法");
            log.error(resEntity.toString());
            return resEntity;
        }

        log.info("用户开始登陆授权： "+ "code: "+code+"    "+"state: "+ state);

        String accessTokenJosnStr = getAccessToken(wx_access_token_url, wx_official_accounts_appid, wx_official_accounts_secret, code);
        log.info("用户： "+ JSONUtil.getStringByKey(accessTokenJosnStr,"openid")+" AccessToken: "+accessTokenJosnStr);

        if(! StringUtils.isBlank(JSONUtil.getStringByKey(accessTokenJosnStr,"errcode"))) {
            resEntity.setReturnCode(10003);
            resEntity.setErrMsg("用户获取access_token失败,Code无效错误");
            log.error(resEntity.toString());
            return resEntity;
        }

        String accessToken = JSONUtil.getStringByKey(accessTokenJosnStr,"access_token");
        String openId = JSONUtil.getStringByKey(accessTokenJosnStr,"openid");
        String userInfoJsonStr = getUserInfoJsonStr(wx_userinfo_url,accessToken,openId);
        log.info("用户：" +openId+" resUserInfo: "+userInfoJsonStr);

        if(! StringUtils.isBlank(JSONUtil.getStringByKey(userInfoJsonStr,"errcode"))) {
            resEntity.setReturnCode(10004);
            resEntity.setErrMsg("用户获取user_info失败,参数错误");
            log.error(resEntity.toString());
            return resEntity;
        }

        JSONObject accessTokenObj = JSONObject.parseObject(accessTokenJosnStr);
        JSONObject userInfoObj = JSONObject.parseObject(userInfoJsonStr);
        JSONObject resJson = new JSONObject();
        resJson.putAll(accessTokenObj);
        resJson.putAll(userInfoObj);

        String resStr = JSONObject.toJSONString(resJson);

        log.info("用户： "+openId+" resJson: "+resJson);

        User user = null;

        user = userMapper.selectUserById(openId,"");
        if(user == null) {
            user = new User();
            user.setOpenId(JSONUtil.getStringByKey(resStr, "openid"));
            user.setSessionKey(JSONUtil.getStringByKey(resStr, "access_token"));
            user.setExpiresIn(System.currentTimeMillis() + 1000*60*60*24*15);
            user.setMemberEndDate(DateUtil.getCurrentDt());
            user.setNickName(JSONUtil.getStringByKey(resStr, "nickname"));
            user.setUserPhoto(JSONUtil.getStringByKey(resStr, "headimgurl"));
            user.setGender(Integer.parseInt(JSONUtil.getStringByKey(resStr, "sex")));
            user.setIsValid(1);
            log.info("{注册用户信息Bean装配完成 结果:" + user.toString() + "}");
        }else {

            if(user.getExpiresIn() <= System.currentTimeMillis()) {
                long expiresIn = System.currentTimeMillis() + 1000*60*60*24*15;
                user.setExpiresIn(expiresIn);
            }
            user.setSessionKey(JSONUtil.getStringByKey(resStr, "access_token"));
            user.setNickName(JSONUtil.getStringByKey(resStr, "nickname"));
            user.setUserPhoto(JSONUtil.getStringByKey(resStr, "headimgurl"));
            user.setGender(Integer.parseInt(JSONUtil.getStringByKey(resStr, "sex")));
        }


        int num = userMapper.insertOrUpdateUser(user);

        if(num == 0) {
            log.error("{用户注册/登陆失败: " + openId + "}");
            resEntity.setReturnCode(10005);
            resEntity.setErrMsg("用户注册/登陆失败");
            log.error(resEntity.toString());
            return resEntity;
        }

        resEntity.setReturnCode(200);
        resEntity.setJsonStr(JSONUtil.bean2JsonStr(user));
        return resEntity;

    }

    //TODO 添加拼团信息

    @Override
    public ResEntity getUserInfo(String openId, String phoneNumber,String sessionKey) {

        ResEntity resEntity = new ResEntity();

        ResEntity authentication = AuthenticationUtil.authenticationSession(openId, sessionKey, userMapper);
        if(authentication.getReturnCode() != 200) {
            return authentication;
        }

//        User user = userMapper.selectUserById(openId,phoneNumber);
        User user = userMapper.selectUserInfoAndOrdersAndGroups(openId);

        resEntity.setReturnCode(200);
        resEntity.setJsonStr(JSONUtil.bean2JsonStr(user));
        return resEntity;
    }

    @Override
    public ResEntity isBanUser(HttpServletRequest request,String openId ,String actionCode) {

        ResEntity resEntity = new ResEntity();

        if(!"0".equals(actionCode) && !"1".equals(actionCode)) {

            resEntity.setReturnCode(400);
            resEntity.setErrMsg("ban用户失败 参数非法 必须为 0 / 1");
            log.info("{ban用户：[" + openId + "] 失败 参数非法 必须为 0 / 1}");
            return resEntity;
        }

        User user = userMapper.selectUserById(openId,"");

        if(user == null) {

            resEntity.setReturnCode(400);
            resEntity.setErrMsg("用户不存在");
            log.info("{用户不存在");
            return resEntity;
        }

        user.setIsValid(Integer.valueOf(actionCode));

        int num = userMapper.insertOrUpdateUser(user);

        if(num == 0) {

            resEntity.setReturnCode(400);
            resEntity.setErrMsg("ban用户失败");
            log.info("{用户状态变更：[" + openId + "] 失败}");
            return resEntity;
        }

        resEntity.setReturnCode(200);
        resEntity.setJsonStr(JSONUtil.bean2JsonStr(user));
        return resEntity;

    }

    @Override
    public ResEntity getAllUser(String pageNum,String pageSize) {

        ResEntity resEntity = new ResEntity();
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(Integer.valueOf(pageNum));
        pageRequest.setPageSize(Integer.valueOf(pageSize));

        PageResult pageResult = PageUtil.getPageResult(pageRequest, getPageInfo(pageRequest));
        List<User> userList = (List<User>) pageResult.getContent();

        if(userList == null || userList.size() == 0) {
            resEntity.setReturnCode(200);
            return resEntity;
        }

        resEntity.setReturnCode(200);
        resEntity.setJsonStr(JSONUtil.bean2JsonStr(pageResult));
        return resEntity;
    }

    @Override
    public ResEntity updateUser(String openId, String sessionKey, String address) {

        ResEntity resEntity = new ResEntity();

        ResEntity authentication = AuthenticationUtil.authenticationSession(openId, sessionKey, userMapper);
        if(authentication.getReturnCode() != 200) {
            return authentication;
        }

        User user = userMapper.selectUserById(openId, "");
        user.setAddress(address);

        int num = userMapper.insertOrUpdateUser(user);

        if(num == 0) {
            log.error("用户 "+ openId +" 修改地址失败 持久化失败");
            resEntity.setReturnCode(10009);
            resEntity.setErrMsg("用户修改地址失败");
            log.error(resEntity.toString());
            return resEntity;
        }

        resEntity.setReturnCode(200);

        return resEntity;

    }


    private PageInfo<User> getPageInfo(PageRequest pageRequest) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        List<User> users = userMapper.selectPage();
        return new PageInfo<User>(users);
    }

    private String getAccessToken(String baseUrl,String appId,String appSecret,String code) throws UnsupportedEncodingException {

        StringBuilder url = new StringBuilder(baseUrl);
        url.append("appid=");
        url.append(appId);
        url.append("&secret=");
        url.append(appSecret);
        url.append("&code=");
        url.append(code);
        url.append("&grant_type=authorization_code");
        String resAccessToken = HttpUtil.doGet(url);
        resAccessToken = new String(resAccessToken.getBytes("ISO-8859-1"), "UTF-8");

        return resAccessToken;
    }

    private String getUserInfoJsonStr(String wxUserinfoUrl, String accessToken, String openId) throws UnsupportedEncodingException {

        StringBuilder url = new StringBuilder(wxUserinfoUrl);
        url.append("access_token=");
        url.append(accessToken);
        url.append("&openid=");
        url.append(openId);
        url.append("&lang=zh_CN");
        String res_content = HttpUtil.doGet(url);
        return new String(res_content.getBytes("ISO-8859-1"), "UTF-8");
    }
}
