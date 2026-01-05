package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.UserReview;
import java.util.List;
import java.util.UUID;

public interface UserReviewDao {
    UserReview findById(UUID id);
    List<UserReview> findAll();
    List<UserReview> findByUserId(UUID userId);
    List<UserReview> findByOrderedProductId(UUID orderedProductId);
    boolean insert(UserReview review);
    boolean update(UserReview review);
    boolean delete(UUID id);
}

