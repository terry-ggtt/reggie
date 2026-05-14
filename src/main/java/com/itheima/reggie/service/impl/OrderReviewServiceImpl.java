package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.OrderReview;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.mapper.OrderReviewMapper;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderReviewService;
import com.itheima.reggie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderReviewServiceImpl extends ServiceImpl<OrderReviewMapper, OrderReview> implements OrderReviewService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    @Transactional
    public void submitReview(OrderReview orderReview) {
        Long userId = BaseContext.getCurrentId();
        Long orderId = orderReview.getOrderId();
        if (orderId == null) {
            throw new CustomException("order not found");
        }

        Orders orders = orderService.getById(orderId);
        if (orders == null || !userId.equals(orders.getUserId())) {
            throw new CustomException("order not found");
        }
        if (!Integer.valueOf(4).equals(orders.getStatus())) {
            throw new CustomException("only completed orders can be reviewed");
        }

        Long orderDetailId = orderReview.getOrderDetailId();
        if (orderDetailId == null) {
            throw new CustomException("order item is required");
        }
        OrderDetail orderDetail = orderDetailService.getById(orderDetailId);
        if (orderDetail == null || !orderId.equals(orderDetail.getOrderId())) {
            throw new CustomException("order item not found");
        }

        LambdaQueryWrapper<OrderReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderReview::getOrderDetailId, orderDetailId);
        queryWrapper.eq(OrderReview::getUserId, userId);
        if (this.count(queryWrapper) > 0) {
            throw new CustomException("order item already reviewed");
        }

        Integer rating = orderReview.getRating();
        if (rating == null || rating < 1 || rating > 5) {
            throw new CustomException("rating must be between 1 and 5");
        }

        orderReview.setUserId(userId);
        orderReview.setDishId(orderDetail.getDishId());
        orderReview.setSetmealId(orderDetail.getSetmealId());
        orderReview.setItemName(orderDetail.getName());
        orderReview.setStatus(1);
        if (orderReview.getIsAnonymous() == null) {
            orderReview.setIsAnonymous(0);
        }
        this.save(orderReview);
    }

    @Override
    @Transactional
    public void replyReview(OrderReview orderReview) {
        if (orderReview.getId() == null) {
            throw new CustomException("review not found");
        }
        OrderReview oldReview = this.getById(orderReview.getId());
        if (oldReview == null) {
            throw new CustomException("review not found");
        }
        OrderReview updateReview = new OrderReview();
        updateReview.setId(orderReview.getId());
        updateReview.setReply(orderReview.getReply());
        updateReview.setReplyTime(LocalDateTime.now());
        updateReview.setReplyUser(BaseContext.getCurrentId());
        this.updateById(updateReview);
    }

    @Override
    @Transactional
    public void updateVisibleStatus(Long id, Integer status) {
        if (id == null || status == null || (status != 0 && status != 1)) {
            throw new CustomException("invalid review status");
        }
        OrderReview oldReview = this.getById(id);
        if (oldReview == null) {
            throw new CustomException("review not found");
        }
        OrderReview updateReview = new OrderReview();
        updateReview.setId(id);
        updateReview.setStatus(status);
        this.updateById(updateReview);
    }
}
