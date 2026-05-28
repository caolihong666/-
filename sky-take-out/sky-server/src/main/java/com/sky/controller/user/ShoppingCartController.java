package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     *
     * 添加购物车
     *
     * @param shoppingCartDTO;
     *
     *
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车，商品信息为：{}", shoppingCartDTO);

        shoppingCartService.addShoppingCart(shoppingCartDTO);

        return Result.success();
    }

    /**
     *
     * 查询购物车
     *
     * @return Result<List<ShoppingCart>>;
     *
     *
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        log.info("查看购物车");
        List<ShoppingCart> list = shoppingCartService.showshoppingCart();

        return Result.success(list);
    }

    /**
     *
     * 清空购物车
     *
     *
     */
    @DeleteMapping("/clean")
    public Result<Void> clean() {
        shoppingCartService.cleanshoppingCart();

        return Result.success();
    }

    /**
     *
     * 删除购物车中一个商品
     *
     * @param shoppingCartDTO ;
     *
     *
     */
    @PostMapping("/sub")
    public Result<Void> sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("删除购物车里其中一个商品，商品信息为：{}", shoppingCartDTO);
        shoppingCartService.subShoppingCart(shoppingCartDTO);

        return Result.success();
    }
}
