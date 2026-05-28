package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     *
     * 统计指定时间区间内的营业额
     *
     * @param begin,end;
     * @return Result<TurnoverReportVO>;
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end之间的所有日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询日期对应的营业额，营业额是指状态为已完成的订单金额总计

            //x年x月x日 00:00:00
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            //x年x月x日 23:59:59
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //订单下单时间在这一天之内 beginTime~endTime
            Map<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        //封装返回结果
        return TurnoverReportVO
                .builder()
                .turnoverList(StringUtils.join(turnoverList, ","))
                .dateList(StringUtils.join(dateList, ","))
                .build();
    }

    /**
     *
     * 统计指定时间区间内的用户数据
     *
     * @param begin,end;
     * @return Result<UserReportVO>;
     *
     *
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //存放从begin到end每天对应的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //统计集合当中每一天所对应的用户数量以及每一天的用户增量

        //存放每一天用户总量的集合 select count(id) from user where create_time<=?;
        List<Integer> totalUserList = new ArrayList<>();

        //存放每一天用户增量的集合 select count(id) from user where create_time>=? and create_time<=?;
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //x年x月x日 00:00:00
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            //x年x月x日 23:59:59
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map<String, Object> map = new HashMap<>();
            map.put("end", endTime);

            //总用户数量
            Integer totalUser = userMapper.countByMap(map);

            map.put("begin", beginTime);

            //新增用户数量
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     *
     * 统计指定时间内的订单数据
     *
     * @param begin ,end;
     * @return Result<OrderReportVO>
     *
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //存放从begin到end每天对应的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的订单总数
        List<Integer> orderCountList = new ArrayList<>();

        //存放每天的有效订单总数
        List<Integer> validOrderCountList = new ArrayList<>();

        //遍历日期集合，查询每天的订单总数和每天的有效订单总数
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //查询每天的订单总数 select count(id) from order where order_time>=? and order_time<=?;
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            //查询每天的有效订单总数 select count(id) from order where order_time>=? and order_time<=? and status=5;
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        //计算时间区间的订单总数量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

        //计算时间区间的有效订单总数量
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        //计算订单完成率
        double orderCompletionRate = 0.0;

        if (totalOrderCount != 0) {
            orderCompletionRate =
                    validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     *
     * 指定时间区间内的销量前十商品统计
     *
     * @param begin ,end;
     * @return Result<SalesTop10ReportVO>;
     *
     *
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //从beginTime~endTime销量前十的商品统计
        List<GoodsSalesDTO> top10 = orderMapper.getTop10(beginTime, endTime);

        List<String> names = top10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = top10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(names, ","))
                .numberList(StringUtils.join(numbers, ","))
                .build();
    }

    /**
     *
     * 导出运营数据报表
     *
     * @param response ;
     *
     *
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //查询数据库获取营业数据----查询最近30天的数据

        //从今天往前30天作为开始日期
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDateTime beginTime = LocalDateTime.of(dateBegin, LocalTime.MIN);

        //昨天作为结束日期
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.of(dateEnd, LocalTime.MAX);

        BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);

        //把查询到的数据写到excel文件当中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //表格文件的sheet标签页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row4 = sheet.getRow(3);
            row4.getCell(2).setCellValue(businessData.getTurnover());
            row4.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row4.getCell(6).setCellValue(businessData.getNewUsers());

            //获得第5行
            XSSFRow row5 = sheet.getRow(4);
            row5.getCell(2).setCellValue(businessData.getValidOrderCount());
            row5.getCell(4).setCellValue(businessData.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                XSSFRow row = sheet.getRow(i + 7);

                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(data.getTurnover());
                row.getCell(3).setCellValue(data.getValidOrderCount());
                row.getCell(4).setCellValue(data.getOrderCompletionRate());
                row.getCell(5).setCellValue(data.getUnitPrice());
                row.getCell(6).setCellValue(data.getNewUsers());
            }

            //通过输出流把将excel文件下载到客户端浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            //关闭资源
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * 根据条件统计订单数量
     *
     * @param begin ,end,status;
     * @return Integer;
     *
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map<String, Object> map = new HashMap<>();

        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);

        return orderMapper.countByMap(map);
    }
}
