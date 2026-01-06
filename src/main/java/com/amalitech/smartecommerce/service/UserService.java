package com.amalitech.smartecommerce.service;

import java.util.UUID;
import java.util.List;

import com.amalitech.smartecommerce.exception.EmailAlreadyExistsException;
import com.amalitech.smartecommerce.model.User;

public interface UserService {
    User getUserById(UUID id);
    List<User> getAllUsers();
    User getUserByEmail(String emailAddress);
    User createUser(User user) throws EmailAlreadyExistsException;
    User updateUser(User user);
    boolean deleteUser(UUID id);
}