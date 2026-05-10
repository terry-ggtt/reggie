package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.DTO.DishDto;
import com.itheima.reggie.DTO.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    public void saveWithDish(SetmealDto setmealDto);
    public void removeWithDish(List<Long> ids);
    public SetmealDto getByIdWithDish(Long id);
    public void updateStatusById(Integer status, List<Long> ids);
    public void updateWithDish(SetmealDto setmealDto);
    public List<DishDto> getDishListBySetmealId(Long id);
}
