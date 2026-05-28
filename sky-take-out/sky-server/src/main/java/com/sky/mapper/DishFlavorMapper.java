package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     *
     * 批量插入口味数据
     *
     * @param flavors;
     *
     *
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     *
     * 删除菜品中关联的口味数据(多条删除)
     *
     * @param ids;
     *
     */
    void deleteByDishIds(List<Long> ids);

    /**
     *
     * 删除菜品中关联的口味数据(单条删除)
     *
     * @param dishId;
     *
     */
    @Delete("delete from dish_flavor where dish_id=#{dishId}")
    void deleteByDishId(Long dishId);

    /**
     *
     * 根据菜品id查询对应的口味数据
     *
     * @param dishId;
     *
     */
    @Select("select * from dish_flavor where dish_id=#{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}
