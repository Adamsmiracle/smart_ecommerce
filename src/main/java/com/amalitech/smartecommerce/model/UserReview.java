package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class UserReview {
    private UUID id;
    private UUID userId;
    private UUID orderedProductId;
    private Integer ratingValue;
    private String comment;

    public UserReview() {}

    public UserReview(UUID id, UUID userId, UUID orderedProductId, Integer ratingValue, String comment) {
        this.id = id;
        this.userId = userId;
        this.orderedProductId = orderedProductId;
        this.ratingValue = ratingValue;
        this.comment = comment;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getOrderedProductId() { return orderedProductId; }
    public void setOrderedProductId(UUID orderedProductId) { this.orderedProductId = orderedProductId; }

    public Integer getRatingValue() { return ratingValue; }
    public void setRatingValue(Integer ratingValue) { this.ratingValue = ratingValue; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}

