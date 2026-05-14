package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.OrderReview;
import com.itheima.reggie.service.OrderReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orderReview")
public class OrderReviewController {

    @Autowired
    private OrderReviewService orderReviewService;

    @PostMapping
    public R<String> submit(@RequestBody OrderReview orderReview) {
        log.info("submit order review: {}", orderReview);
        orderReviewService.submitReview(orderReview);
        return R.success("review submitted");
    }

    @GetMapping("/myPage")
    public R<Page> myPage(int page, int pageSize) {
        Page<OrderReview> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderReview::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(OrderReview::getCreateTime);
        orderReviewService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, Long orderId, Integer ratingMax, Integer status) {
        Page<OrderReview> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(orderId != null, OrderReview::getOrderId, orderId);
        queryWrapper.le(ratingMax != null, OrderReview::getRating, ratingMax);
        queryWrapper.eq(status != null, OrderReview::getStatus, status);
        queryWrapper.orderByAsc(OrderReview::getRating);
        queryWrapper.orderByDesc(OrderReview::getCreateTime);
        orderReviewService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    @GetMapping("/public")
    public R<List<OrderReview>> publicList(Long dishId, Long setmealId) {
        LambdaQueryWrapper<OrderReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderReview::getStatus, 1);
        queryWrapper.eq(dishId != null, OrderReview::getDishId, dishId);
        queryWrapper.eq(setmealId != null, OrderReview::getSetmealId, setmealId);
        queryWrapper.orderByDesc(OrderReview::getCreateTime);
        queryWrapper.last("limit 20");
        return R.success(orderReviewService.list(queryWrapper));
    }

    @PutMapping("/reply")
    public R<String> reply(@RequestBody OrderReview orderReview) {
        log.info("reply order review: {}", orderReview);
        orderReviewService.replyReview(orderReview);
        return R.success("review replied");
    }

    @PutMapping("/status")
    public R<String> status(@RequestParam Long id, @RequestParam Integer status) {
        orderReviewService.updateVisibleStatus(id, status);
        return R.success("review status updated");
    }
}
