package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单明细数据
     *
     * @param orderDetailList;
     *
     *
     *
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     *
     * 通过订单id查询出订单明细
     *
     * @param ordersId ;
     * @return List<OrderDetail>
     *
     */
    @Select("select * from order_detail where order_id=#{ordersId}")
    List<OrderDetail> getByOrderId(Long ordersId);
}
