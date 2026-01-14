package com.amalitech.smartecommerce.service;

import java.util.List;
import java.util.UUID;
import com.amalitech.smartecommerce.model.UserReview;


public interface UserReviewService {
    UserReview deleteUserReview(UUID id);

    UserReview updateUserReview(UserReview review);

    UserReview createUserReview(UserReview review);

    List<UserReview> getUserReviewsByOrderedProductId(UUID orderedProductId);

    List<UserReview> getUserReviewsByUserId(UUID userId);

    List<UserReview> getAllUserReviews();

    UserReview getUserReviewById(UUID id);
}
