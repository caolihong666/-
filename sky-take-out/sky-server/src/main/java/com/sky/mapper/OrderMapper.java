package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper
public interface OrderMapper {

    /**
     *
     * 插入订单数据
     *
     * @param orders ;
     *
     *
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders;
     */
    void update(Orders orders);

    /**
     *
     * 用于替换微信支付更新数据库的状态问题
     *
     * @param orderStatus;
     * @param orderPaidStatus ;
     *
     */
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} " +
            "where number = #{orderNumber}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, String orderNumber);

    /**
     *
     * 分页条件查询并按照下单时间进行排序
     *
     * @param ordersPageQueryDTO ;
     * @return Page<Orders>
     *
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     *
     * @param id;
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /**
     * 各个状态的订单数量统计
     *
     * @return Integer;
     */
    @Select("select count(id) from orders where status=#{status}")
    Integer countStatus(Integer status);

    /**
     *
     * 查询未付款并且超时的订单
     *
     */
    @Select("select * from orders where status=#{status} and #{orderTime}>order_time")
    List<Orders> getByStatusAndOrdersTimeout(Integer status, LocalDateTime orderTime);

    /**
     *
     * 根据动态条件查询营业额数据
     *
     * @param map;
     * @return Double;
     */
    Double sumByMap(Map<String, Object> map);

    /**
     *
     * 根据动态条件统计订单数量
     *
     * @param map;
     * @return Integer;
     */
    Integer countByMap(Map<String, Object> map);

    /**
     *
     * 统计指定时间区间内销量前十的商品和数量
     *
     * @param begin ,end;
     * @return List<GoodsSalesDTO>;
     *
     */
    List<GoodsSalesDTO> getTop10(LocalDateTime begin, LocalDateTime end);
}
