package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     *
     * 根据菜品id查询套餐id
     *
     *
     */
    List<Long> getSetmealDishIds(List<Long> ids);

    /**
     *
     *
     * 批量保存菜品和套餐的关系
     *
     *
     *
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id查询套餐菜品关联数据
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);

    /**
     *
     * 根据套餐id删除菜品关系表中的数据
     *
     *
     *
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deteBySetmealId(Long setmealId);
}
