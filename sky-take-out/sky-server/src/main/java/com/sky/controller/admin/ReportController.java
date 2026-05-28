package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/*
 * 数据统计相关接口
 */
@RestController
@RequestMapping("/admin/report")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     *
     * 统计营业额
     *
     * @param begin,end;
     * @return Result<TurnoverReportVO>;
     */
    @GetMapping("turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        log.info("营业额数据统计：{}，{}", begin, end);

        TurnoverReportVO reportVO = reportService.getTurnoverStatistics(begin, end);

        return Result.success(reportVO);
    }

    /**
     *
     * 统计用户数据
     *
     * @param begin,end;
     * @return Result<UserReportVO>;
     *
     *
     */
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        log.info("用户数据统计：{}，{}", begin, end);

        UserReportVO reportVO = reportService.getUserStatistics(begin, end);

        return Result.success(reportVO);
    }

    /**
     *
     * 统计订单数据
     *
     * @param begin ,end;
     * @return Result<OrderReportVO>
     *
     */
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("订单数据统计：{}，{}", begin, end);

        OrderReportVO reportVO = reportService.getOrdersStatistics(begin, end);

        return Result.success(reportVO);
    }

    /**
     *
     * 销量前十商品统计
     *
     * @param begin ,end;
     * @return Result<SalesTop10ReportVO>;
     *
     *
     */
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("销量前十商品统计：{}，{}", begin, end);

        SalesTop10ReportVO reportVO = reportService.getTop10(begin, end);

        return Result.success(reportVO);
    }

    /**
     *
     * 导出运营数据报表
     *
     * @param response ;
     *
     *
     */
    @GetMapping("/export")
    public Result<Void> export(HttpServletResponse response) {
        reportService.exportBusinessData(response);

        return Result.success();
    }
}
