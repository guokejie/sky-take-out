package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {
    void save(AddressBook addressBook);

    List<AddressBook> list(AddressBook addressBook);


    void update(AddressBook addressBook);

    void deleteById(Integer id);

    AddressBook getById(Long id);

    void setDefault(AddressBook addressBook);
}
