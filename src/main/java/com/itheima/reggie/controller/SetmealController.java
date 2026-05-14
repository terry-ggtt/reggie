package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.DTO.DishDto;
import com.itheima.reggie.DTO.SetmealDto;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;

    @PostMapping
    @Transactional
    public R< String> save(@RequestBody SetmealDto setmeal){
        log.info("新增套餐");
        setmealService.saveWithDish(setmeal);

        return R.success("新增套餐成功");
    }
    @GetMapping("/page")
    @Transactional
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);
//        对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }
    @DeleteMapping
    @Transactional
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        log.info("id:{}",id);
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }
    @PostMapping("/status/{status}")
    @Transactional
    public R<String> status(@PathVariable int status, @RequestParam List<Long> ids){
        log.info("status:{}",status);
        log.info("ids:{}",ids);
        setmealService.updateStatusById(status,ids);
        return R.success("更新成功");
    }
    @PutMapping
    @Transactional
    public R<String> update(@RequestBody SetmealDto setmeal){
        log.info("修改套餐");
        setmealService.updateWithDish(setmeal);
        return R.success("修改套餐成功");
    }

    @GetMapping("list")
    public R<List<SetmealDto>> list( Setmeal setmeal){
        log.info("list:{}",setmeal);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        Map<Long, Integer> monthlySales = getMonthlySetmealSales();
        List<SetmealDto> result = list.stream().map(item -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            setmealDto.setSaleNum(monthlySales.getOrDefault(item.getId(), 0));
            return setmealDto;
        }).collect(Collectors.toList());

        return R.success(result);
    }
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> getDishListBySetmealId(@PathVariable Long id){
        log.info("查询套餐id为{}的菜品列表", id);
        List<DishDto> list = setmealService.getDishListBySetmealId(id);
        return R.success(list);
    }
    private Map<Long, Integer> getMonthlySetmealSales() {
        LocalDateTime beginTime = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.ge(Orders::getOrderTime, beginTime);
        orderWrapper.le(Orders::getOrderTime, LocalDateTime.now());
        orderWrapper.in(Orders::getStatus, 2, 3, 4);
        List<Orders> orders = orderService.list(orderWrapper);
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> orderIds = orders.stream().map(Orders::getId).collect(Collectors.toList());
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.in(OrderDetail::getOrderId, orderIds);
        detailWrapper.isNotNull(OrderDetail::getSetmealId);
        List<OrderDetail> details = orderDetailService.list(detailWrapper);

        Map<Long, Integer> result = new HashMap<>();
        for (OrderDetail detail : details) {
            result.merge(detail.getSetmealId(), detail.getNumber() == null ? 0 : detail.getNumber(), Integer::sum);
        }
        return result;
    }
}
