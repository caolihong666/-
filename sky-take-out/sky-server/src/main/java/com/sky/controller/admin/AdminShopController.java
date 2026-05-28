package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
@Slf4j
public class AdminShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     *
     * 设置店铺营业状态
     *
     * @param status ;
     *
     **/
    @PutMapping("/{status}")
    public Result<Void> setStatus(@PathVariable Integer status) {
        log.info("设置店铺的营业状态为：{}", status == 1 ? "营业中" : "打烊中");

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();

        ops.set(KEY, status);

        return Result.success();
    }

    /**
     *
     * 管理端查询店铺营业状态
     *
     * @return Result.success(status);
     *
     */
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();

        Integer status = (Integer) ops.get(KEY);

        if (status != null) {
            log.info("获取到店铺的营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        }

        return Result.success(status);
    }
}
