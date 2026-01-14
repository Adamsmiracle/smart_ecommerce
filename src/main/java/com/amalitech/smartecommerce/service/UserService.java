package com.amalitech.smartecommerce.service;

import java.sql.SQLException;
import java.util.UUID;
import java.util.List;

import com.amalitech.smartecommerce.exception.EmailAlreadyExistsException;
import com.amalitech.smartecommerce.model.User;

public interface UserService {
    User getUserById(UUID id);
    List<User> getAllUsers() throws SQLException;
    User getUserByEmail(String emailAddress);
    User createUser(User user) throws EmailAlreadyExistsException, SQLException;
    User updateUser(User user) throws SQLException;
    User deleteUser(UUID id) throws SQLException;
}