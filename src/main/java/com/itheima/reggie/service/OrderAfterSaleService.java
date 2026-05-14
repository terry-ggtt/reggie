package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.OrderAfterSale;

public interface OrderAfterSaleService extends IService<OrderAfterSale> {

    void apply(OrderAfterSale afterSale);

    void handle(OrderAfterSale afterSale);
}
