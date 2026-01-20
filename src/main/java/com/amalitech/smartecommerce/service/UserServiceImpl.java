package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.UserDao;
import com.amalitech.smartecommerce.dao.UserDaoImpl;
import com.amalitech.smartecommerce.model.User;
import com.amalitech.smartecommerce.utils.UserUtils;
import com.amalitech.smartecommerce.exception.EmailAlreadyExistsException;


import java.sql.SQLException;
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
    public List<User> getAllUsers() throws SQLException {
        return userDao.findAll();
    }

    @Override
    public User getUserByEmail(String emailAddress) {
        return userDao.findByEmail(emailAddress);
    }

    @Override
    public User createUser(User user) throws EmailAlreadyExistsException, SQLException {
        // Basic validations
        if (user == null) return null;
        String email = user.getEmailAddress();
        String password = user.getPassword();
        if (email == null || email.trim().isEmpty()) return null;
        if (password == null || password.trim().isEmpty()) return null;

        // Ensure email is unique
        if (userDao.findByEmail(email) != null) {
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
            return user;
        }

        return userDao.create(user);
    }



    @Override
    public User updateUser(User user) throws SQLException {
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
        // If a plain password was provided, hash it before updating
        String pwd = user.getPassword();
        if (pwd != null && !pwd.trim().isEmpty()) {
            // If it doesn't look like a bcrypt hash, hash it
            if (!(pwd.startsWith("$2a$") || pwd.startsWith("$2b$") || pwd.startsWith("$2y$"))) {
                try {
                    String hashed = UserUtils.hashPassword(pwd);
                    user.setPassword(hashed);
                } catch (RuntimeException ex) {
                    throw new SQLException("Failed to hash password", ex);
                }
            }
        }

        // Delegate to DAO; DAO returns null on failure so wrap and propagate exception for callers
        User updated = userDao.update(user);
        if (updated == null) {
            throw new SQLException("Failed to update user with id: " + user.getId());
        }
        return updated;
    }



    @Override
    public User deleteUser(UUID id) throws SQLException {
        if (id == null){
            throw new IllegalArgumentException("User ID cannot be null for deletion");
        }
        return userDao.delete(id);
    }

}
