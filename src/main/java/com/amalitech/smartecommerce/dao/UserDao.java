package com.amalitech.smartecommerce.dao;

import java.util.UUID;
import java.util.List;
import com.amalitech.smartecommerce.model.User;

public interface UserDao {
    boolean delete(UUID id);
    User update(User user);
    User insert(User user);
    User findByEmail(String emailAddress);
    List<User> findAll();
    User findById(UUID id);
}
