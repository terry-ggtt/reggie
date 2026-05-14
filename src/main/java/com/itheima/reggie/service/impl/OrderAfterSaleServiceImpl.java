package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.OrderAfterSale;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.mapper.OrderAfterSaleMapper;
import com.itheima.reggie.service.OrderAfterSaleService;
import com.itheima.reggie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderAfterSaleServiceImpl extends ServiceImpl<OrderAfterSaleMapper, OrderAfterSale> implements OrderAfterSaleService {

    @Autowired
    private OrderService orderService;

    @Override
    @Transactional
    public void apply(OrderAfterSale afterSale) {
        Long userId = BaseContext.getCurrentId();
        Long orderId = afterSale.getOrderId();
        if (orderId == null) {
            throw new CustomException("order not found");
        }

        Orders orders = orderService.getById(orderId);
        if (orders == null || !userId.equals(orders.getUserId())) {
            throw new CustomException("order not found");
        }
        if (!Integer.valueOf(4).equals(orders.getStatus())) {
            throw new CustomException("only completed orders can apply after-sale service");
        }
        if (afterSale.getType() == null || (afterSale.getType() != 1 && afterSale.getType() != 2)) {
            throw new CustomException("invalid after-sale type");
        }
        if (afterSale.getReason() == null || afterSale.getReason().trim().isEmpty()) {
            throw new CustomException("after-sale reason is required");
        }
        if (afterSale.getApplyAmount() == null || afterSale.getApplyAmount().compareTo(BigDecimal.ZERO) < 0) {
            afterSale.setApplyAmount(BigDecimal.ZERO);
        }
        if (afterSale.getApplyAmount().compareTo(orders.getAmount()) > 0) {
            throw new CustomException("apply amount cannot exceed order amount");
        }

        LambdaQueryWrapper<OrderAfterSale> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderAfterSale::getOrderId, orderId);
        queryWrapper.eq(OrderAfterSale::getUserId, userId);
        queryWrapper.in(OrderAfterSale::getStatus, 1, 2);
        if (this.count(queryWrapper) > 0) {
            throw new CustomException("order has pending after-sale request");
        }

        afterSale.setUserId(userId);
        afterSale.setStatus(1);
        afterSale.setApplyTime(LocalDateTime.now());
        this.save(afterSale);
    }

    @Override
    @Transactional
    public void handle(OrderAfterSale afterSale) {
        if (afterSale.getId() == null) {
            throw new CustomException("after-sale request not found");
        }
        OrderAfterSale oldAfterSale = this.getById(afterSale.getId());
        if (oldAfterSale == null) {
            throw new CustomException("after-sale request not found");
        }
        Integer status = afterSale.getStatus();
        if (status == null || status < 2 || status > 4) {
            throw new CustomException("invalid after-sale status");
        }
        if (Integer.valueOf(4).equals(oldAfterSale.getStatus())) {
            throw new CustomException("after-sale request already completed");
        }

        BigDecimal handleAmount = afterSale.getHandleAmount();
        if (handleAmount != null && handleAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("handle amount cannot be negative");
        }
        if (handleAmount != null) {
            Orders orders = orderService.getById(oldAfterSale.getOrderId());
            if (orders != null && handleAmount.compareTo(orders.getAmount()) > 0) {
                throw new CustomException("handle amount cannot exceed order amount");
            }
        }

        OrderAfterSale updateAfterSale = new OrderAfterSale();
        updateAfterSale.setId(afterSale.getId());
        updateAfterSale.setStatus(status);
        updateAfterSale.setHandleAmount(handleAmount);
        updateAfterSale.setHandleRemark(afterSale.getHandleRemark());
        updateAfterSale.setHandleTime(LocalDateTime.now());
        updateAfterSale.setHandleUser(BaseContext.getCurrentId());
        this.updateById(updateAfterSale);
    }
}
