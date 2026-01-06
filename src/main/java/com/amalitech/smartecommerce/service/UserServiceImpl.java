package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.UserDao;
import com.amalitech.smartecommerce.dao.UserDaoImpl;
import com.amalitech.smartecommerce.model.User;
import com.amalitech.smartecommerce.utils.UserUtils;
import com.amalitech.smartecommerce.exception.EmailAlreadyExistsException;


import java.util.List;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

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
    public User createUser(User user) throws EmailAlreadyExistsException {
        // Basic validations
        if (user == null) return null;
        String email = user.getEmailAddress();
        String password = user.getPassword();
        if (email == null || email.trim().isEmpty()) return null;
        if (password == null || password.trim().isEmpty()) return null;

        // Ensure email is unique
        if (userDao.findByEmail(email) != null) {
            // Instead of returning null, throw a specific exception so callers (controllers)
            throw new EmailAlreadyExistsException(email);
        }
        // Ensure id is set
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }
        // Hash password before persisting
        try {
            String hashedPassword = UserUtils.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return null;
        }

        return userDao.insert(user);
    }



    @Override
    public User updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null for update");
        }
        String userId = user.getId().toString();
        if (userId.trim().isEmpty()){
            throw new IllegalArgumentException("user ID cannot be empty");
        }
        return userDao.update(user);
    }



    @Override
    public boolean deleteUser(UUID id) {
        if (id == null){
            throw new IllegalArgumentException("User ID cannot be null for deletion");
        }
        return userDao.delete(id);
    }

}
