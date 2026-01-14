package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.UserReviewDao;
import com.amalitech.smartecommerce.dao.UserReviewDaoImpl;
import com.amalitech.smartecommerce.model.UserReview;

import java.util.List;
import java.util.UUID;

public class UserReviewServiceImpl implements UserReviewService {
    private final UserReviewDao userReviewDao;

    public UserReviewServiceImpl() {
        this.userReviewDao = new UserReviewDaoImpl();
    }

    public UserReviewServiceImpl(UserReviewDao userReviewDao) {
        this.userReviewDao = userReviewDao;
    }

    @Override
    public UserReview getUserReviewById(UUID id) {
        return userReviewDao.findById(id);
    }

    @Override
    public List<UserReview> getAllUserReviews() {
        return userReviewDao.findAll();
    }

    @Override
    public List<UserReview> getUserReviewsByUserId(UUID userId) {
        return userReviewDao.findByUserId(userId);
    }

    @Override
    public List<UserReview> getUserReviewsByOrderedProductId(UUID orderedProductId) {
        return userReviewDao.findByOrderedProductId(orderedProductId);
    }

    @Override
    public UserReview createUserReview(UserReview review) {
        return userReviewDao.create(review);
    }

    @Override
    public UserReview updateUserReview(UserReview review) {
        return userReviewDao.update(review);
    }

    @Override
    public UserReview deleteUserReview(UUID id) {
        return userReviewDao.delete(id);
    }
}

