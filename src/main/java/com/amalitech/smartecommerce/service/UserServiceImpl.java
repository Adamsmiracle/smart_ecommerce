package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.UserDao;
import com.amalitech.smartecommerce.dao.UserDaoImpl;
import com.amalitech.smartecommerce.model.User;

import java.util.List;
import java.util.UUID;

public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    public UserServiceImpl() {
        this.userDao = new UserDaoImpl();
    }

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User getUserById(UUID id) {
        return userDao.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public User getUserByEmail(String emailAddress) {
        return userDao.findByEmail(emailAddress);
    }

    @Override
    public User createUser(User user) {
        return userDao.insert(user);
    }

    @Override
    public User updateUser(User user) {
        return userDao.update(user);
    }

    @Override
    public boolean deleteUser(UUID id) {
        return userDao.delete(id);
    }
}
