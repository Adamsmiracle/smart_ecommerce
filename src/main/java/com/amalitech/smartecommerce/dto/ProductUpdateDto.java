package com.amalitech.smartecommerce.dto;

import com.amalitech.smartecommerce.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for updating an existing product.
 * Includes price and stock which are stored in product_item table.
 */
public class ProductUpdateDto {

    @NotNull(message = ValidationMessages.PRODUCT_ID_REQUIRED)
    private UUID id;

    @NotBlank(message = ValidationMessages.PRODUCT_NAME_REQUIRED)
    @Size(min = 1, max = 255, message = ValidationMessages.PRODUCT_NAME_SIZE)
    private String name;

    @Size(max = 2000, message = ValidationMessages.PRODUCT_DESCRIPTION_SIZE)
    private String description;

    @NotNull(message = ValidationMessages.CATEGORY_ID_REQUIRED)
    private UUID categoryId;

    private String productImage;

    @Positive(message = ValidationMessages.PRODUCT_PRICE_POSITIVE)
    private double price;

    @PositiveOrZero(message = ValidationMessages.PRODUCT_STOCK_POSITIVE)
    private int stock;

    public ProductUpdateDto() {}

    public ProductUpdateDto(UUID id, String name, String description, UUID categoryId, String productImage, double price, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.productImage = productImage;
        this.price = price;
        this.stock = stock;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
