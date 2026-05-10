package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.DTO.DishDto;
import com.itheima.reggie.DTO.SetmealDto;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public R<List<Setmeal>> list( Setmeal setmeal){
        log.info("list:{}",setmeal);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> getDishListBySetmealId(@PathVariable Long id){
        log.info("查询套餐id为{}的菜品列表", id);
        List<DishDto> list = setmealService.getDishListBySetmealId(id);
        return R.success(list);
    }

}
