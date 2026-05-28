package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    /**
     *
     * 新增套餐
     *
     * @param setmealDTO ;
     *
     *
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     *
     * 删除套餐，起售中的套餐不能删除
     * @param ids ;
     *
     */
    void deleteBatch(List<Long> ids);

    /**
     *
     * 根据id查询套餐
     *
     * @param id;
     * @return Result<Setmeal>;
     *
     */
    SetmealVO getByIdWithDish(Long id);

    /**
     * 修改套餐
     */
    void updateWithDish(SetmealDTO setmealDTO);

    /**
     * 修改套餐起售状态
     */
    void startOrStop(Integer status, Long id);

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
