package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {

    // 插入订单数据
    void insert(Orders orders);


    @Select("select * from order where number=#{orderNumber} and user_id=#{userId}")
    Orders getByNumber(String orderNumber, Long userId);



    void update(Orders orders);
}
