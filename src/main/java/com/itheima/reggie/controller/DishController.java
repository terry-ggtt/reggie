package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.DTO.DishDto;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @PostMapping
    public R <String> save(@RequestBody DishDto dish){
        log.info("dish = {}",dish);
        dishService.saveWithFlavor(dish);
        return R.success("新增菜品成功");

    }
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>(); 

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }
//    删除
//    @DeleteMapping
//    public R<String> delete(@RequestParam List<Long> ids) {
//        log.info("删除的id为：{}", ids);
//        dishService.removeWithFlsh();
//        return R.success("删除成功");
//    }
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("修改的分类信息为：{}", dishDto);
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
    }
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable int status, @RequestParam List<Long> ids) {
        log.info("status = {}, ids = {}", status, ids);
        dishService.updateStatusByIds(status, ids);
        return R.success("修改成功");
    }
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
//        构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                dishDto.setCategoryName(category.getName());

            }
            Long dishId = item.getId();
            List<DishFlavor> dishFlavorList = dishFlavorService.list(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, dishId));
            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtoList);
    }
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("删除的id为：{}", ids);
        dishService.removeWithFlavor(ids);
        return R.success("删除成功");
    }

}
