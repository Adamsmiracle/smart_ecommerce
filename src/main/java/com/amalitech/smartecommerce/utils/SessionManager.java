package com.amalitech.smartecommerce.utils;

import com.amalitech.smartecommerce.model.User;

/**
 * Session manager to track logged-in user state.
 */
public class SessionManager {
    private static SessionManager instance;

    private User currentUser;
    private boolean isAdmin;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        currentUser = null;
        isAdmin = false;
    }

    public String getUserDisplayName() {
        if (currentUser != null) {
            return currentUser.getFirstName() + " " + currentUser.getLastName();
        }
        return "Guest";
    }
}

