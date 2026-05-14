package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.OrderAfterSale;
import com.itheima.reggie.service.OrderAfterSaleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/afterSale")
public class OrderAfterSaleController {

    @Autowired
    private OrderAfterSaleService orderAfterSaleService;

    @PostMapping
    public R<String> apply(@RequestBody OrderAfterSale afterSale) {
        log.info("apply after-sale service: {}", afterSale);
        orderAfterSaleService.apply(afterSale);
        return R.success("after-sale request submitted");
    }

    @GetMapping("/myPage")
    public R<Page> myPage(int page, int pageSize, Integer status) {
        Page<OrderAfterSale> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderAfterSale> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderAfterSale::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(status != null, OrderAfterSale::getStatus, status);
        queryWrapper.orderByDesc(OrderAfterSale::getApplyTime);
        orderAfterSaleService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, Long orderId, Integer type, Integer status) {
        Page<OrderAfterSale> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderAfterSale> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(orderId != null, OrderAfterSale::getOrderId, orderId);
        queryWrapper.eq(type != null, OrderAfterSale::getType, type);
        queryWrapper.eq(status != null, OrderAfterSale::getStatus, status);
        queryWrapper.orderByAsc(OrderAfterSale::getStatus);
        queryWrapper.orderByDesc(OrderAfterSale::getApplyTime);
        orderAfterSaleService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    @PutMapping("/handle")
    public R<String> handle(@RequestBody OrderAfterSale afterSale) {
        log.info("handle after-sale service: {}", afterSale);
        orderAfterSaleService.handle(afterSale);
        return R.success("after-sale request handled");
    }
}
