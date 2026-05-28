package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    /**
     *
     * 用户下单(生成订单，添加到数据库)
     *
     * @param ordersSubmitDTO ;
     * @return Result<OrderSubmitVO>;
     *
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {

        //处理各种业务异常（地址簿为空，购物车为空）用地址id查询地址是否为空？用用户id查询购物车是否为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> cart = shoppingCartMapper.list(shoppingCart);

        if (cart == null || cart.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

        orderMapper.insert(orders);

        //向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart Cart : cart) {
            OrderDetail orderDetail = new OrderDetail();

            BeanUtils.copyProperties(Cart, orderDetail);

            orderDetail.setOrderId(orders.getId());

            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

        //用户下单成功，清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        //封装vo返回结果
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "餐饮商家一体化管理系统订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED; //订单状态，待接单

        //发现没有将支付时间 check_out属性赋值，所以在这里更新
        LocalDateTime check_out_time = LocalDateTime.now();

        //获取订单号码
        String orderNumber = ordersPaymentDTO.getOrderNumber();

        log.info("调用updateStatus，用于替换微信支付更新数据库状态的问题");

        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderNumber);

        //通过websocket向浏览器推送信息
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1); //1 来单提醒 2 客户催单
        Orders orders = orderMapper.getByNumber(orderNumber);
        map.put("orderId", orders.getId());
        map.put("content", "订单号：" + orderNumber);

        String json = JSON.toJSONString(map);

        webSocketServer.sendToAllClient(json);

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 用户端订单分页查询
     *
     * @param pageNum;
     * @param pageSize;
     * @param status;
     * @return PageResult;
     */
    @Override
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        //封装查询条件，必须是该用户的订单
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        //封装查询条件，必须是该状态
        ordersPageQueryDTO.setStatus(status);

        //分页条件查询
        //select * from order where id=#{userId} and status=#{status}
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();

        //查询出订单明细，并封装入OrderVO进行响应
        if (page != null && !page.isEmpty()) {
            //如果有满足查询条件的订单
            for (Orders orders : page) {
                //获取订单id
                Long ordersId = orders.getId();

                //通过订单id查询出订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(ordersId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }

        //总条数，结果列表
        return new PageResult(page.getTotal(), list);
    }

    /**
     *
     * 查询订单详情
     *
     * @param id ;
     * @return Result<OrderVO>;
     *
     */
    @Override
    public OrderVO details(Long id) {
        //根据id查询订单
        Orders orders = orderMapper.getById(id);

        //查询该订单对应的菜品/套餐明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        //封装vo并返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     *
     * 取消订单
     *
     * @param id ;
     *
     *
     */
    @Override
    public void userCancelById(Long id) throws Exception {
        //根据id查询订单
        Orders orderDB = orderMapper.getById(id);

        //如果订单不存在，抛出异常
        if (orderDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //如果status>2,要么订单不能取消，要么就是不能在这个接口取消
        if (orderDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //构造订单更新对象（仅包含需要修改的字段）
        Orders orders = new Orders();
        orders.setId(id);

        //订单处于待接单的状态下取消，需要退款
        if (orderDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            // TODO 模拟退款（当前无微信支付环境）
            log.info("模拟退款成功，订单号：{}", orderDB.getNumber());

            //支付状态修改为退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间 update操作
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     *
     * 再来一单
     *
     * @param orderId ;
     *
     *
     */
    @Override
    public void repetition(Long orderId) {
        //获取用户id
        Long userId = BaseContext.getCurrentId();

        //校验前端传的orderId是否为该用户的orderId
        Orders orders = orderMapper.getById(orderId);

        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if (!orders.getUserId().equals(userId)) {
            //如果不是，代表前端传过来的orderId有误，抛出异常
            throw new OrderBusinessException("非法操作");
        }

        //根据id查询订单明细
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

        List<ShoppingCart> list = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        if (orderDetails != null && !orderDetails.isEmpty()) {
            //把订单明细对象转换成购物车对象
            for (OrderDetail orderDetail : orderDetails) {
                ShoppingCart shoppingCart = new ShoppingCart();

                //补充必须的字段信息到购物车对象当中
                BeanUtils.copyProperties(orderDetail, shoppingCart);
                shoppingCart.setUserId(userId);
                shoppingCart.setCreateTime(now);

                list.add(shoppingCart);
            }
        }

        //批量复制到购物车表当中
        if (!list.isEmpty()) {
            shoppingCartMapper.insertBatchShoppingCart(list);
        }
    }

    /**
     * 条件搜索订单
     *
     * @param ordersPageQueryDTO;
     * @return Result<PageResult>;
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //分页查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();

        //返回OrderVO集合
        if (page != null && !page.isEmpty()) {
            for (Orders orders : page) {
                OrderVO orderVO = new OrderVO();

                BeanUtils.copyProperties(orders, orderVO);

                //获取订单菜品信息
                String orderDishes = getStringOrderDishes(orders);

                orderVO.setOrderDishes(orderDishes);

                list.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), list);
    }

    /**
     * 各个状态的订单数量统计
     *
     * @return Result<OrderStatisticsVO>;
     */
    @Override
    public OrderStatisticsVO statistics() {
        //根据状态，分别查询出待接单，待派送，派送中的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        //封装到VO类中返回
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     *
     * 接单
     *
     * @param ordersConfirmDTO ;
     *
     *
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = new Orders();
        //把状态修改为已接单
        orders.setStatus(Orders.CONFIRMED);
        orders.setId(ordersConfirmDTO.getId());

        orderMapper.update(orders);
    }

    /**
     *
     * 拒单
     *
     * @param ordersRejectionDTO ;
     *
     *
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //根据id查询订单
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //创建修改的orders对象
        Orders orders1 = new Orders();
        orders1.setId(ordersRejectionDTO.getId());
        orders1.setStatus(Orders.CANCELLED);
        orders1.setRejectionReason(ordersRejectionDTO.getRejectionReason());

        //如果用户已经支付，需要退款
        if (orders.getPayStatus().equals(Orders.PAID)) {
            //支付状态修改为退款
            orders1.setPayStatus(Orders.REFUND);

            // TODO 模拟退款（当前无微信支付环境）
            log.info("模拟退款成功，订单号：{}", orders.getNumber());
        }

        orderMapper.update(orders1);
    }

    /**
     *
     * 商家取消订单
     *
     * @param ordersCancelDTO ;
     *
     *
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        //根据id查询订单
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());

        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Orders orders1 = new Orders();

        //如果订单支付状态为已支付，需退款
        if (orders.getPayStatus().equals(Orders.PAID)) {
            //支付状态修改为退款
            orders1.setPayStatus(Orders.REFUND);

            // TODO 模拟退款（当前无微信支付环境）
        }

        //修改取消信息
        orders1.setCancelTime(LocalDateTime.now());
        orders1.setCancelReason(ordersCancelDTO.getCancelReason());
        orders1.setStatus(Orders.CANCELLED);
        orders1.setId(ordersCancelDTO.getId());

        orderMapper.update(orders1);
    }

    /**
     *
     * 派送订单
     *
     * @param id ;
     *
     *
     */
    @Override
    public void delivery(Long id) {
        //根据id查询订单
        Orders orders = orderMapper.getById(id);

        //校验订单是否存在,订单状态是否为待派送
        if (orders == null || !orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //订单状态改成派送中
        Orders orders1 = new Orders();
        orders1.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orders1.setId(id);

        orderMapper.update(orders1);
    }

    /**
     *
     * 完成订单
     *
     * @param id ;
     *
     */
    @Override
    public void complete(Long id) {
        //根据id查询订单
        Orders orders = orderMapper.getById(id);

        //校验订单是否存在，且是否为派送中
        if (orders == null || !orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //将订单状态修改为已完成
        Orders orders1 = new Orders();
        orders1.setId(id);
        orders1.setStatus(Orders.COMPLETED);
        orders1.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders1);
    }

    /**
     *
     * 客户催单
     *
     * @param id ;
     *
     */
    @Override
    public void reminder(Long id) {
        //根据id查询订单
        Orders orders = orderMapper.getById(id);

        //校验订单是否存在，且是否为待接单
        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //实现催单功能
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);//1 来单提醒 2 客户催单
        map.put("orderId", id);
        map.put("content", "订单号：" + orders.getNumber());

        String json = JSON.toJSONString(map);

        //通过webSocketServer向客户端推送消息
        webSocketServer.sendToAllClient(json);
    }

    /**
     *
     * 获取订单菜品信息的方法
     *
     * @param orders ;
     *
     */
    private String getStringOrderDishes(Orders orders) {
        //根据orderId查询订单详细信息
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());

        if (orderDetails == null || orderDetails.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        //拼接为字符串
        for (OrderDetail orderDetail : orderDetails) {
            sb.append(orderDetail.getName())
                    .append("*")
                    .append(orderDetail.getNumber())
                    .append(";");
        }

        return sb.toString();
    }
}
