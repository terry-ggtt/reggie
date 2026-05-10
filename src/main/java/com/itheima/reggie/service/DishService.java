package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.DTO.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dish);
    public DishDto getByIdWithFlavor(Long id);
    public void updateWithFlavor(DishDto dishDto);
    public void updateStatusByIds(Integer status, List<Long> ids);
    public void removeWithFlavor(List<Long> ids);
}
