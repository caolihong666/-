package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     *
     * 根据用户唯一标识查询用户是否存在
     *
     * @param openid;
     * @return User;
     *
     */
    @Select("select * from user where openid=#{openid}")
    User getByOpenId(String openid);

    /**
     *
     * 添加用户
     *
     * @param user;
     *
     * */
    void insert(User user);

    /**
     *
     * 根据user表主键查询用户信息
     *
     * @param userId ;
     *
     * */
    @Select("select * from user where id=#{userId}")
    User getById(Long userId);

    /**
     *
     *根据动态条件统计用户数量
     *
     *
     * @return Integer;
     * */
    Integer countByMap(Map<String,Object> map);
}
