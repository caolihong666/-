package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@Slf4j
@RequestMapping("/admin/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索
     *
     * @param ordersPageQueryDTO;
     * @return Result<PageResult>;
     */
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计
     *
     * @return Result<OrderStatisticsVO>;
     */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics() {
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();

        return Result.success(orderStatisticsVO);
    }

    /**
     *
     * 查询订单详情
     *
     * @param id ;
     * @return Result<OrderVO>;
     *
     *
     */
    @GetMapping("/details/{id}")
    public Result<OrderVO> details(@PathVariable Long id) {
        OrderVO details = orderService.details(id);

        return Result.success(details);
    }

    /**
     *
     * 接单
     *
     * @param ordersConfirmDTO ;
     *
     *
     */
    @PutMapping("/confirm")
    public Result<Void> confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        orderService.confirm(ordersConfirmDTO);

        return Result.success();
    }

    /**
     *
     * 拒单
     *
     * @param ordersRejectionDTO ;
     *
     *
     */
    @PutMapping("/rejection")
    public Result<Void> rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        orderService.rejection(ordersRejectionDTO);

        return Result.success();
    }

    /**
     *
     * 取消订单
     *
     * @param ordersCancelDTO ;
     * @return Result.success();
     *
     */
    @PutMapping("/cancel")
    public Result<Void> cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        orderService.cancel(ordersCancelDTO);

        return Result.success();
    }

    /**
     *
     * 派送订单
     *
     * @param id ;
     *
     *
     */
    @PutMapping("/delivery/{id}")
    public Result<Void> delivery(@PathVariable Long id) {
        orderService.delivery(id);

        return Result.success();
    }

    /**
     *
     * 完成订单
     *
     * @param id ;
     *
     */
    @PutMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable("id") Long id) {
        orderService.complete(id);

        return Result.success();
    }
}
