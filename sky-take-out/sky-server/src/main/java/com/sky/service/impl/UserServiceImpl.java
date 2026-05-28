package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    //微信服务接口地址
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     *
     * 微信小程序登录接口
     *
     * @param userLoginDTO;
     * @return Result<UserLoginVO>;
     *
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //调用微信服务器接口，返回用户唯一标识
        String openid = getOpenId(userLoginDTO);

        //判断用户唯一标识是否为空，为空表示登录失败，抛出业务异常
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //查询用户是否在用户表中
        User user = userMapper.getByOpenId(openid);

        //用户不在用户表中，帮其注册，返回用户
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            //返回主键
            userMapper.insert(user);
        }

        //用户在用户表中，返回用户
        return user;
    }

    /**
     *
     * 调用微信接口服务，获取用户的唯一用户标识
     *
     * @param userLoginDTO;
     * @return String;
     *
     *
     */
    private String getOpenId(UserLoginDTO userLoginDTO) {
        //调用微信服务器接口，返回用户唯一标识
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        JSONObject jsonObject = JSON.parseObject(json);

        return jsonObject.getString("openid");
    }
}
