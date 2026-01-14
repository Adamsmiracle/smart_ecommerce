package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.UserReview;
import java.util.List;
import java.util.UUID;

public interface UserReviewDao extends DAO<UserReview> {
    List<UserReview> findByUserId(UUID userId);
    List<UserReview> findByOrderedProductId(UUID orderedProductId);
}

