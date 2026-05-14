package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.OrderReview;

public interface OrderReviewService extends IService<OrderReview> {

    void submitReview(OrderReview orderReview);

    void replyReview(OrderReview orderReview);

    void updateVisibleStatus(Long id, Integer status);
}
