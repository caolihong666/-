package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     *
     * 新增菜品
     *
     * @param dishDTO;
     *
     */
    @PostMapping
    @CacheEvict(cacheNames = "dishCache", key = "#dishDTO.categoryId")
    public Result<Void> save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        return Result.success();
    }

    /**
     *
     * 菜品分页查询
     *
     * @param dishPageQueryDTO;
     * @return Result<PageResult>;
     *
     *
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     *
     * 菜品批量删除
     *
     * @param ids;
     *
     */
    @DeleteMapping
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<Void> delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除:{}", ids);

        dishService.deleteBatch(ids);

        return Result.success();
    }

    /**
     *
     *
     * 根据id查询菜品
     *
     * @param id ;
     *
     */
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     *
     * 修改菜品信息
     *
     * @param dishDTO;
     *
     */
    @PutMapping
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<Void> update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);

        dishService.updateWithFlavor(dishDTO);

        return Result.success();
    }

    /**
     *
     * 修改菜品售卖状态
     *
     * @param status,id;
     *
     */
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<Void> startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        log.info("根据id:{}修改菜品的状态{}", id, status);
        dishService.startOrStop(status, id);

        return Result.success();
    }

    /**
     *
     *
     * 根据分类id查询菜品
     *
     * @param categoryId ;
     * @return Result<List<Dish>>;
     *
     */
    @GetMapping("/list")
    public Result<List<Dish>> list(@RequestParam Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);

        List<Dish> list = dishService.list(categoryId);

        return Result.success(list);
    }
}
