package com.amalitech.smartecommerce.cache;

import com.amalitech.smartecommerce.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory cache for users with O(1) lookups.
 * Provides fast retrieval by ID and email, plus search capabilities.
 */
public class UserCache {
    private static UserCache instance;

    // Primary cache: id -> User (mirrors primary key index)
    private final Map<UUID, User> userById;

    // Secondary index: email (lowercase) -> User (mirrors email unique index)
    private final Map<String, User> userByEmail;

    // All users list for iteration
    private List<User> allUsers;

    // Cache statistics
    private long cacheHits = 0;
    private long cacheMisses = 0;
    private long lastRefreshTime = 0;

    private UserCache() {
        this.userById = new ConcurrentHashMap<>();
        this.userByEmail = new ConcurrentHashMap<>();
        this.allUsers = new ArrayList<>();
    }

    public static synchronized UserCache getInstance() {
        if (instance == null) {
            instance = new UserCache();
        }
        return instance;
    }

    /**
     * Load all users into cache, building indexes.
     */
    public void loadAll(List<User> users) {
        clear();
        this.allUsers = new ArrayList<>(users);

        for (User user : users) {
            // Zero out password before caching to avoid exposing hashes in memory
            if (user.getPassword() != null) user.setPassword(null);
            // Primary index
            userById.put(user.getId(), user);

            // Email index
            if (user.getEmailAddress() != null) {
                userByEmail.put(user.getEmailAddress().toLowerCase(), user);
            }
        }

        lastRefreshTime = System.currentTimeMillis();
    }

    /**
     * Get user by ID - O(1) lookup.
     */
    public User getById(UUID id) {
        User user = userById.get(id);
        if (user != null) {
            cacheHits++;
        } else {
            cacheMisses++;
        }
        return user;
    }

    /**
     * Get user by email - O(1) lookup.
     */
    public User getByEmail(String email) {
        if (email == null) {
            cacheMisses++;
            return null;
        }
        User user = userByEmail.get(email.toLowerCase());
        if (user != null) {
            cacheHits++;
        } else {
            cacheMisses++;
        }
        return user;
    }

    /**
     * Get all users.
     */
    public List<User> getAll() {
        cacheHits++;
        return new ArrayList<>(allUsers);
    }

    /**
     * Search users by name or email.
     */
    public List<User> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAll();
        }

        String searchTerm = query.toLowerCase().trim();
        cacheHits++;

        return allUsers.stream()
            .filter(user -> {
                String email = user.getEmailAddress() != null ? user.getEmailAddress().toLowerCase() : "";
                String firstName = user.getFirstName() != null ? user.getFirstName().toLowerCase() : "";
                String lastName = user.getLastName() != null ? user.getLastName().toLowerCase() : "";
                String fullName = (firstName + " " + lastName).trim();

                return email.contains(searchTerm) ||
                       firstName.contains(searchTerm) ||
                       lastName.contains(searchTerm) ||
                       fullName.contains(searchTerm);
            })
            .collect(Collectors.toList());
    }

    /**
     * Add a user to the cache.
     */
    public void put(User user) {
        if (user == null || user.getId() == null) return;

        // Avoid storing password hash in cache
        if (user.getPassword() != null) user.setPassword(null);
         userById.put(user.getId(), user);

         if (user.getEmailAddress() != null) {
             userByEmail.put(user.getEmailAddress().toLowerCase(), user);
         }

         // Add to list if not already present
         if (!allUsers.contains(user)) {
             allUsers.add(user);
         }
     }

     /**
      * Remove a user from the cache.
      */
     public void remove(UUID id) {
        User user = userById.remove(id);
        if (user != null) {
            allUsers.remove(user);
            if (user.getEmailAddress() != null) {
                userByEmail.remove(user.getEmailAddress().toLowerCase());
            }
        }
    }

    /**
     * Update a user in the cache.
     */
    public void update(User user) {
         if (user == null || user.getId() == null) return;

        // Avoid storing password hash in cache
        if (user.getPassword() != null) user.setPassword(null);
         // Get the old user to remove old email index
         User oldUser = userById.get(user.getId());
         if (oldUser != null && oldUser.getEmailAddress() != null) {
             userByEmail.remove(oldUser.getEmailAddress().toLowerCase());
         }

         // Update primary index
         userById.put(user.getId(), user);

         // Update email index
         if (user.getEmailAddress() != null) {
             userByEmail.put(user.getEmailAddress().toLowerCase(), user);
         }

         // Update in list
         int index = -1;
         for (int i = 0; i < allUsers.size(); i++) {
             if (allUsers.get(i).getId().equals(user.getId())) {
                 index = i;
                 break;
             }
         }
         if (index >= 0) {
             allUsers.set(index, user);
         } else {
             allUsers.add(user);
         }
    }

    /**
     * Check if email exists in cache.
     */
    public boolean emailExists(String email) {
        if (email == null) return false;
        return userByEmail.containsKey(email.toLowerCase());
    }

    /**
     * Check if email exists for a different user (for update validation).
     */
    public boolean emailExistsForOtherUser(String email, UUID userId) {
        if (email == null) return false;
        User existing = userByEmail.get(email.toLowerCase());
        return existing != null && !existing.getId().equals(userId);
    }

    /**
     * Clear the cache.
     */
    public void clear() {
        userById.clear();
        userByEmail.clear();
        allUsers.clear();
        cacheHits = 0;
        cacheMisses = 0;
    }

    /**
     * Get cache hit rate as percentage.
     */
    public double getHitRate() {
        long total = cacheHits + cacheMisses;
        return total > 0 ? (cacheHits * 100.0 / total) : 0;
    }

    // Getters for statistics
    public long getCacheHits() { return cacheHits; }
    public long getCacheMisses() { return cacheMisses; }
    public int getSize() { return allUsers.size(); }
    public long getLastRefreshTime() { return lastRefreshTime; }
}

