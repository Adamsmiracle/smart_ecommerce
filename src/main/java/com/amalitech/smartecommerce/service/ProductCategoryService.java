package com.amalitech.smartecommerce.service;

import java.util.List;
import java.util.UUID;
import com.amalitech.smartecommerce.model.ProductCategory;


public interface ProductCategoryService {
    boolean deleteCategory(UUID id);

    boolean updateCategory(ProductCategory category);

    boolean createCategory(ProductCategory category);

    List<ProductCategory> getAllCategories();

    ProductCategory getCategoryById(UUID id);
}



