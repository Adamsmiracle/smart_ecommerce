package com.amalitech.smartecommerce.service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import com.amalitech.smartecommerce.model.ProductCategory;


public interface ProductCategoryService {
    ProductCategory deleteCategory(UUID id) throws SQLException;

    ProductCategory updateCategory(ProductCategory category) throws SQLException;

    ProductCategory createCategory(ProductCategory category) throws SQLException;

    List<ProductCategory> getAllCategories() throws SQLException;

    ProductCategory getCategoryById(UUID id);
}



