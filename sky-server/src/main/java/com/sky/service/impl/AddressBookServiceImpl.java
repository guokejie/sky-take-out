package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;


    @Override
    public void save(AddressBook addressBook) {
        // 这个东西前端不会传给我们，需要自己设置
        addressBook.setUserId(BaseContext.getCurrentId());
        // 默认情况下，不设置为默认地址
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }

    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        return addressBookMapper.list(addressBook);
    }

    @Override
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    @Override
    public void deleteById(Integer id) {
        addressBookMapper.deleteById(id);
    }

    @Override
    public AddressBook getById(Long id) {
        AddressBook addressBook = addressBookMapper.getById(id);
        return addressBook;
    }

    @Override
    public void setDefault(AddressBook addressBook) {
        // 1.将当前用户的所有地址都设置为非默认地址
        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updatesDefaultByUserId(addressBook);
        // 2.将当前地址改为默认地址
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }
}
