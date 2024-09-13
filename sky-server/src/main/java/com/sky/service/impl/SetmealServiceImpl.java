package com.sky.service.impl;

import com.github.pagehelper.Constant;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    // 套餐菜品关联表
    @Autowired
    private SetMealDishMapper setMealDishMapper;

    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemById(id);
    }

    /**
     * 新增套餐，需要同时保存套餐和菜品的关联关系
     *
     * @param setmealDTO
     */
    // TODO
    @Override
    @Transactional // 因为涉及到1张表以上
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 保存套餐
        setmealMapper.insert(setmeal);

        // 套餐id
        Long setmealId = setmeal.getId();
        // 套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId); // 设置每一个菜属于哪个套餐
        });

        setMealDishMapper.insertBatch(setmealDishes);


    }

    // TODO：分页查询
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        int pageNum = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    // TODO：批量删除套餐
    @Override
    @Transactional
    // 无法精确清理，因为这里是ids，全部清理
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            // 根据id查询这个套餐
            Setmeal setmeal = setmealMapper.getById(id);
            // 判断其是否处于起售阶段，如果是起售则不能删除
            if (StatusConstant.ENABLE.equals(setmeal.getStatus())) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
            // 允许删除
            ids.forEach(setmealId -> {
                // 删除套餐中的数据
                setmealMapper.deleteById(setmealId);
                // 删除套餐菜品关系表中的数据
                setMealDishMapper.deleteBySetmealId(setmealId);
            });
        });

    }


}
