package com.amalitech.smartecommerce.dao;

import java.util.UUID;
import java.util.List;
import com.amalitech.smartecommerce.model.User;

public interface UserDao extends DAO<User> {
    User findByEmail(String emailAddress);
}
