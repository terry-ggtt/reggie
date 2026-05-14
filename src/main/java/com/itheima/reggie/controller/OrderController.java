 package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

 /**
  * 订单
  */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }
     @GetMapping("/page")
     public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime){
         log.info("page={}, pageSize={}, number={}, beginTime={}, endTime={}", page, pageSize, number, beginTime, endTime);
         Page<Orders> pageInfo = new Page<>(page, pageSize);
         LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
         queryWrapper.like(number != null, Orders::getNumber, number);
         queryWrapper.ge(beginTime != null, Orders::getOrderTime, beginTime);
         queryWrapper.le(endTime != null, Orders::getOrderTime, endTime);
         queryWrapper.orderByDesc(Orders::getOrderTime);
         Page<Orders> ordersPage = orderService.page(pageInfo, queryWrapper);
         return R.success(ordersPage);
     }
     @GetMapping("/userPage")
     public R<Page> userPage(int page, int pageSize){
         log.info("用户订单分页查询：page={}, pageSize={}", page, pageSize);
         Page<Orders> pageInfo = new Page<>(page, pageSize);

         LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
         Long currentId = BaseContext.getCurrentId();
         queryWrapper.eq(Orders::getUserId, currentId);
         queryWrapper.orderByDesc(Orders::getOrderTime);

         Page<Orders> ordersPage = orderService.page(pageInfo, queryWrapper);
         fillOrderDetails(ordersPage.getRecords());
         return R.success(ordersPage);
     }

     @GetMapping("/list")
     public R<List<Orders>> list(){
         LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
         queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
         queryWrapper.orderByDesc(Orders::getOrderTime);
         List<Orders> ordersList = orderService.list(queryWrapper);
         fillOrderDetails(ordersList);
         return R.success(ordersList);
     }

     @PutMapping
     public R<String> update(@RequestBody Orders orders){
         log.info("订单数据：{}",orders);
         orderService.updateById(orders);
         return R.success("修改成功");
     }
    @PostMapping("/again")
     public R<String> again(@RequestBody Orders orders){
         log.info("订单数据：{}",orders);
         if (orders.getId() == null) {
             return R.error("订单不存在");
         }
         Orders oldOrder = orderService.getById(orders.getId());
         if (oldOrder == null || !BaseContext.getCurrentId().equals(oldOrder.getUserId())) {
             return R.error("订单不存在");
         }
         orderService.again(orders);
         return R.success("重新下单成功");
     }

     private void fillOrderDetails(List<Orders> ordersList) {
         if (ordersList == null || ordersList.isEmpty()) {
             return;
         }
         for (Orders orders : ordersList) {
             LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
             detailWrapper.eq(OrderDetail::getOrderId, orders.getId());
             List<OrderDetail> orderDetails = orderDetailService.list(detailWrapper);
             orders.setOrderDetails(orderDetails);
             int sumNum = 0;
             for (OrderDetail orderDetail : orderDetails) {
                 if (orderDetail.getNumber() != null) {
                     sumNum += orderDetail.getNumber();
                 }
             }
             orders.setSumNum(sumNum);
         }
     }

 }
