package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;

import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析
 */
@Slf4j
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 按日统计销售数据
     */
    @GetMapping("/sales/date")
    public R<List<Map>> salesByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("按日统计：begin={}, end={}", begin, end);

        List<Map> result = new ArrayList<>();
        LocalDate current = begin;
        while (!current.isAfter(end)) {
            LocalDateTime startOfDay = current.atTime(0, 0, 0);
            LocalDateTime endOfDay = current.atTime(23, 59, 59);

            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.between(Orders::getOrderTime, startOfDay, endOfDay);
            queryWrapper.eq(Orders::getStatus, 4);

            List<Orders> orders = orderService.list(queryWrapper);

            int orderCount = orders.size();
            BigDecimal totalAmount = orders.stream()
                    .map(Orders::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", current.toString());
            dayData.put("orderCount", orderCount);
            dayData.put("totalAmount", totalAmount);
            result.add(dayData);

            current = current.plusDays(1);
        }
        return R.success(result);
    }

    /**
     * 按月统计销售数据
     */
    @GetMapping("/sales/month")
    public R<List<Map>> salesByMonth(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") String begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") String end) {
        log.info("按月统计：begin={}, end={}", begin, end);

        List<String> months = getMonthRange(begin, end);
        List<Map> result = new ArrayList<>();

        for (String month : months) {
            LocalDate firstDay = LocalDate.parse(month + "-01");
            LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

            LocalDateTime startOfMonth = firstDay.atTime(0, 0, 0);
            LocalDateTime endOfMonth = lastDay.atTime(23, 59, 59);

            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.between(Orders::getOrderTime, startOfMonth, endOfMonth);
            queryWrapper.eq(Orders::getStatus, 4);

            List<Orders> orders = orderService.list(queryWrapper);

            int orderCount = orders.size();
            BigDecimal totalAmount = orders.stream()
                    .map(Orders::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month);
            monthData.put("orderCount", orderCount);
            monthData.put("totalAmount", totalAmount);
            result.add(monthData);
        }
        return R.success(result);
    }

    /**
     * 按年统计销售数据
     */
    @GetMapping("/sales/year")
    public R<List<Map>> salesByYear(
            @RequestParam String begin,
            @RequestParam String end) {
        log.info("按年统计：begin={}, end={}", begin, end);

        List<Map> result = new ArrayList<>();
        int startYear = Integer.parseInt(begin);
        int endYear = Integer.parseInt(end);

        for (int year = startYear; year <= endYear; year++) {
            LocalDate firstDay = LocalDate.of(year, 1, 1);
            LocalDate lastDay = LocalDate.of(year, 12, 31);

            LocalDateTime startOfYear = firstDay.atTime(0, 0, 0);
            LocalDateTime endOfYear = lastDay.atTime(23, 59, 59);

            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.between(Orders::getOrderTime, startOfYear, endOfYear);
            queryWrapper.eq(Orders::getStatus, 4);

            List<Orders> orders = orderService.list(queryWrapper);

            int orderCount = orders.size();
            BigDecimal totalAmount = orders.stream()
                    .map(Orders::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> yearData = new HashMap<>();
            yearData.put("year", year);
            yearData.put("orderCount", orderCount);
            yearData.put("totalAmount", totalAmount);
            result.add(yearData);
        }
        return R.success(result);
    }

    /**
     * 热销菜品TOP10
     */
    @GetMapping("/dish/top10")
    public R<List<Map>> topDishes(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("热销菜品：begin={}, end={}, limit={}", begin, end, limit);

        LocalDateTime startTime = begin.atTime(0, 0, 0);
        LocalDateTime endTime = end.atTime(23, 59, 59);

        // 查询时间范围内的订单
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.between(Orders::getOrderTime, startTime, endTime);
        orderWrapper.eq(Orders::getStatus, 4);
        List<Orders> orders = orderService.list(orderWrapper);
        List<Long> orderIds = orders.stream().map(Orders::getId).collect(Collectors.toList());

        if (orderIds.isEmpty()) {
            return R.success(new ArrayList<>());
        }

        // 查询这些订单的菜品详情
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.in(OrderDetail::getOrderId, orderIds);
        detailWrapper.isNotNull(OrderDetail::getDishId);
        List<OrderDetail> details = orderDetailService.list(detailWrapper);

        // 按菜品ID分组统计销量
        Map<Long, Integer> dishSales = details.stream()
                .collect(Collectors.groupingBy(
                        OrderDetail::getDishId,
                        Collectors.summingInt(OrderDetail::getNumber)
                ));

        List<Long> dishIds = new ArrayList<>(dishSales.keySet());
        if (dishIds.isEmpty()) {
            return R.success(new ArrayList<>());
        }

        // 批量查询菜品信息
        List<Dish> dishes = dishService.listByIds(dishIds);
        Map<Long, Dish> dishMap = dishes.stream()
                .collect(Collectors.toMap(Dish::getId, d -> d));

        List<Map> result = dishSales.entrySet().stream()
                .map(entry -> {
                    Long dishId = entry.getKey();
                    Integer sales = entry.getValue();
                    Dish dish = dishMap.get(dishId);
                    Map<String, Object> item = new HashMap<>();
                    item.put("dishId", dishId);
                    item.put("name", dish != null ? dish.getName() : "未知菜品");
                    item.put("sales", sales);
                    return item;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("sales"), (Integer) a.get("sales")))
                .limit(limit)
                .collect(Collectors.toList());

        return R.success(result);
    }

    /**
     * 分类销售占比
     */
    @GetMapping("/category/sales")
    public R<List<Map>> categorySales(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("分类销售：begin={}, end={}", begin, end);

        LocalDateTime startTime = begin.atTime(0, 0, 0);
        LocalDateTime endTime = end.atTime(23, 59, 59);

        // 查询时间范围内的订单
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.between(Orders::getOrderTime, startTime, endTime);
        orderWrapper.eq(Orders::getStatus, 4);
        List<Orders> orders = orderService.list(orderWrapper);
        List<Long> orderIds = orders.stream().map(Orders::getId).collect(Collectors.toList());

        if (orderIds.isEmpty()) {
            return R.success(new ArrayList<>());
        }

        // 查询订单明细
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.in(OrderDetail::getOrderId, orderIds);
        List<OrderDetail> details = orderDetailService.list(detailWrapper);

        // 获取所有分类
        List<Category> categories = categoryService.list();
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // 获取所有菜品
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        List<Dish> dishes = dishService.list(dishWrapper);
        Map<Long, Long> dishCategoryMap = dishes.stream()
                .collect(Collectors.toMap(Dish::getId, Dish::getCategoryId));

        // 按分类统计销售额
        Map<Long, BigDecimal> categoryAmounts = new HashMap<>();
        for (OrderDetail detail : details) {
            Long dishId = detail.getDishId();
            if (dishId != null && dishCategoryMap.containsKey(dishId)) {
                Long categoryId = dishCategoryMap.get(dishId);
                categoryAmounts.merge(categoryId, detail.getAmount(), BigDecimal::add);
            }
        }

        // 转换为结果列表
        List<Map> result = categoryAmounts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("categoryId", entry.getKey());
                    item.put("categoryName", categoryMap.getOrDefault(entry.getKey(), "未知分类"));
                    item.put("amount", entry.getValue());
                    return item;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")))
                .collect(Collectors.toList());

        return R.success(result);
    }

    /**
     * 获取月份范围
     */
    private List<String> getMonthRange(String begin, String end) {
        List<String> months = new ArrayList<>();
        LocalDate current = LocalDate.parse(begin + "-01");
        LocalDate endDate = LocalDate.parse(end + "-01");
        while (!current.isAfter(endDate)) {
            months.add(current.toString().substring(0, 7));
            current = current.plusMonths(1);
        }
        return months;
    }
}