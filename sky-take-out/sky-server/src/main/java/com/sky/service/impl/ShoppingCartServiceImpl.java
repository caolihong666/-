package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     *
     * 添加购物车
     *
     * @param shoppingCartDTO;
     *
     *
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //当前添加到购物车的商品是否在购物车中存在，执行select操作
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        //获取用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list != null && !list.isEmpty()) {
            //如果存在，商品数量+1，执行update操作
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateByNumber(cart);
        } else {
            //如果不存在，执行insert操作

            //判断本次添加到购物车的是菜品还是套餐
            Long dishId = shoppingCart.getDishId();

            if (dishId != null) {
                //本次添加到购物车的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                //本次添加到购物车的是套餐
                Long setmealId = shoppingCart.getSetmealId();

                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     *
     * 查询购物车
     *
     * @return Result<List<ShoppingCart>>;
     *
     *
     */
    @Override
    public List<ShoppingCart> showshoppingCart() {
        //获取当前微信用户的id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart cart = ShoppingCart.builder()
                .userId(userId)
                .build();

        return shoppingCartMapper.list(cart);
    }

    /**
     *
     * 清空购物车
     *
     *
     */
    @Override
    public void cleanshoppingCart() {
        //获取当前微信用户的id
        Long userId = BaseContext.getCurrentId();

        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     *
     * 删除购物车中一个商品
     *
     * @param shoppingCartDTO ;
     *
     *
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //设置查询条件，查询出购物车里要操作的商品记录
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);

            Integer number = cart.getNumber();

            //如果该商品数量=1，该商品删除，执行delete操作
            if (number == 1) {
                //能在商品表中被查出来，那么就代表一定有id
                shoppingCartMapper.deleteById(cart.getId());
            } else {
                //如果该商品数量>1,商品数量-1，执行update操作
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateByNumber(cart);
            }
        }
    }
}
