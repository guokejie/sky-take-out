package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
public class DishController {

    @Autowired
    private DishService dishService;


    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId) {

        // TODO：复习
        // 构造redis中的key,规则：dish_分类id
        String key = "dish" + categoryId;

        // 查询redis中是否存在菜品数据
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key); // 放进去什么类型，取出来什么类型
        if (list != null && list.size() > 0) {
            // 如果存在，直接返回，无需查询数据库
            return Result.success(list);
        }
        // 如果不存在，查询数据库，将查询到的数据放入到redis中
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);

        list = dishService.listWithFlavor(dish); // 这一步已经得到了数据
        redisTemplate.opsForValue().set(key, list); // 把它放到redis中
        return Result.success(list);
    }
}
